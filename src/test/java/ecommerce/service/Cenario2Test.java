package ecommerce.service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.fake.repository.FakeCarrinhoRepository;
import ecommerce.fake.repository.FakeClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static ecommerce.util.MetodosAuxilar.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Cenario2Test {

    @Mock
    private IEstoqueExternal estoqueMock;
    @Mock
    private IPagamentoExternal pagamentoMock;

    private FakeClienteRepository clienteRepositoryFake;
    private FakeCarrinhoRepository carrinhoRepositoryFake;

    private ClienteService clienteService;
    private CarrinhoDeComprasService carrinhoService;
    private CompraService compraService;

    @BeforeEach
    void setup() {
        clienteRepositoryFake = new FakeClienteRepository();
        carrinhoRepositoryFake = new FakeCarrinhoRepository();

        clienteService = new ClienteService(clienteRepositoryFake);
        carrinhoService = new CarrinhoDeComprasService(carrinhoRepositoryFake);

        compraService = new CompraService(carrinhoService, clienteService, estoqueMock, pagamentoMock);
    }

    @Test
    @DisplayName("Compra finalizada com sucesso quando estoque e pagamentos estão ok")
    void deveFinalizarCompraComSucesso() {
        Cliente cliente = criarCliente(1L, "Lucas");
        clienteRepositoryFake.adicionar(cliente);

        CarrinhoDeCompras carrinho = criarCarrinho(
                criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );
        carrinho.setId(10L);
        carrinho.setCliente(cliente);
        carrinhoRepositoryFake.adicionar(carrinho);

        when(estoqueMock.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));

        when(pagamentoMock.autorizarPagamento(anyLong(), anyDouble()))
                .thenReturn(new PagamentoDTO(true, 12345L));

        when(estoqueMock.darBaixa(anyList(), anyList())).thenReturn(new EstoqueBaixaDTO(true));

        CompraDTO resultado = compraService.finalizarCompra(10L, 1L);

        assertTrue(resultado.sucesso());
        assertEquals("Compra finalizada com sucesso.", resultado.mensagem());

        verify(pagamentoMock).autorizarPagamento(1L, 100.00);

        verify(estoqueMock).verificarDisponibilidade(
                eq(List.of(1L)),
                eq(List.of(1L))
        );

        verify(estoqueMock).darBaixa(
                eq(List.of(1L)),
                eq(List.of(1L))
        );
    }

    @Test
    @DisplayName("Compra não finalizada pois o cliente não foi encontrado")
    void deveFalharSeClienteNaoEncontrado() {
        Cliente cliente = criarCliente(1L, "Lucas");

        CarrinhoDeCompras carrinho = criarCarrinho(
                criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );
        carrinho.setId(50L);
        carrinho.setCliente(cliente);
        carrinhoRepositoryFake.adicionar(carrinho);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> compraService.finalizarCompra(50L, 1L));

        assertEquals("Cliente não encontrado", e.getMessage());
    }

    @Test
    @DisplayName("Compra não finalizada porque carrinho não pertence ao cliente")
    void deveFalharSeCarrinhoNaoPertenceAoCliente() {
        Cliente cliente1 = criarCliente(1L, "Lucas");
        Cliente cliente2 = criarCliente(2L, "Carlos");

        clienteRepositoryFake.adicionar(cliente1);

        CarrinhoDeCompras carrinho = criarCarrinho();
        carrinho.setId(50L);
        carrinho.setCliente(cliente2);
        carrinhoRepositoryFake.adicionar(carrinho);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compraService.finalizarCompra(50L, 1L));

        assertEquals("Carrinho não encontrado.", ex.getMessage());
    }

    @Test
    @DisplayName("Compra não finalizada por falta de estoque")
    void deveFalharQuandoSemEstoque() {
        Cliente cliente = criarCliente(1L, "Lucas");
        clienteRepositoryFake.adicionar(cliente);

        CarrinhoDeCompras carrinho = criarCarrinho(
                criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );
        carrinho.setId(10L);
        carrinho.setCliente(cliente);
        carrinhoRepositoryFake.adicionar(carrinho);

        when(estoqueMock.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(false, Collections.emptyList()));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> compraService.finalizarCompra(10L, 1L));

        assertEquals("Itens fora de estoque.", e.getMessage());
    }

    @Test
    @DisplayName("Compra não finalizada por pagamento não autorizado")
    void deveFalharQuandoPagamentoRecusado() {
        Cliente cliente = criarCliente(1L, "Lucas");
        clienteRepositoryFake.adicionar(cliente);

        CarrinhoDeCompras carrinho = criarCarrinho(
                criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );
        carrinho.setId(10L);
        carrinho.setCliente(cliente);
        carrinhoRepositoryFake.adicionar(carrinho);

        when(estoqueMock.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));

        when(pagamentoMock.autorizarPagamento(anyLong(), anyDouble()))
                .thenReturn(new PagamentoDTO(false, 12345L));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> compraService.finalizarCompra(10L, 1L));

        assertEquals("Pagamento não autorizado.",  e.getMessage());
    }

    @Test
    @DisplayName("Compra não finalizada por erro ao dar baixa")
    void deveFalharQuandoBaixaNaoRealizada() {
        Cliente cliente = criarCliente(1L, "Lucas");
        clienteRepositoryFake.adicionar(cliente);

        CarrinhoDeCompras carrinho = criarCarrinho(
                criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );
        carrinho.setId(10L);
        carrinho.setCliente(cliente);
        carrinhoRepositoryFake.adicionar(carrinho);

        when(estoqueMock.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));

        when(pagamentoMock.autorizarPagamento(anyLong(), anyDouble()))
                .thenReturn(new PagamentoDTO(true, 12345L));

        when(estoqueMock.darBaixa(anyList(), anyList()))
                .thenReturn(new EstoqueBaixaDTO(false));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> compraService.finalizarCompra(10L, 1L));

        assertEquals("Erro ao dar baixa no estoque.", e.getMessage());
        verify(pagamentoMock).cancelarPagamento(eq(1L), eq(12345L));
    }

}
