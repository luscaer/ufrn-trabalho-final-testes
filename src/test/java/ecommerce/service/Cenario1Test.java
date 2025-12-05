package ecommerce.service;

import ecommerce.dto.CompraDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.external.fake.EstoqueSimulado;
import ecommerce.external.fake.PagamentoSimulado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static ecommerce.util.MetodosAuxilar.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Cenario1Test {

    @Mock
    private CarrinhoDeComprasService carrinhoDeComprasService;
    @Mock
    private ClienteService clienteService;

    private EstoqueSimulado estoqueFake;
    private PagamentoSimulado pagamentoFake;

    private CompraService compraService;

    @BeforeEach
    void setUp() {
        estoqueFake = new EstoqueSimulado();
        pagamentoFake = new PagamentoSimulado();
        compraService = new CompraService(carrinhoDeComprasService, clienteService, estoqueFake, pagamentoFake);
    }

    @Test
    @DisplayName("Compra finalizada com sucesso quando estoque e pagamentos estão ok")
    void deveFinalizarCompraSucesso() {
        Cliente cliente = criarCliente(1L, "Lucas");
        CarrinhoDeCompras carrinho = criarCarrinho(
                criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoDeComprasService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        estoqueFake.setDisponivel(true);
        pagamentoFake.setAutorizado(true);

        CompraDTO resultado = compraService.finalizarCompra(1L, 1L);

        assertEquals(1L, estoqueFake.getIdsRecebidos().get(0),
                "O ID enviado para o estoque está errado!");
        assertEquals(1L, estoqueFake.getQntdsRecebidas().get(0),
                "A quantidade enviada para o estoque está errada!");

        assertTrue(resultado.sucesso());
        assertEquals("Compra finalizada com sucesso.", resultado.mensagem());
    }

    @Test
    @DisplayName("Compra não finalizada por falta de estoque")
    void deveFalharQuandoSemEstoque() {
        Cliente cliente = criarCliente(1L, "Lucas");
        CarrinhoDeCompras carrinho = criarCarrinho(
                criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoDeComprasService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        estoqueFake.setDisponivel(false);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> compraService.finalizarCompra(1L, 1L));

        assertEquals("Itens fora de estoque.", e.getMessage());
    }

    @Test
    @DisplayName("Compra não finalizada por pagamento não autorizado")
    void deveFalharQuandoPagamentoRecusado() {
        Cliente cliente = criarCliente(1L, "Carlos");
        CarrinhoDeCompras carrinho = criarCarrinho(
            criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoDeComprasService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        estoqueFake.setDisponivel(true);
        pagamentoFake.setAutorizado(false);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> compraService.finalizarCompra(1L, 1L));

        assertEquals("Pagamento não autorizado.", e.getMessage());
    }

    @Test
    @DisplayName("Compra não finalizada por erro ao dar baixa")
    void deveFalharQuandoBaixaNaoRealizada() {
        Cliente cliente = criarCliente(1L, "Carlos");
        CarrinhoDeCompras carrinho = criarCarrinho(
                criarItem(new BigDecimal("100.00"), new BigDecimal("1.00"), 1L)
        );

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoDeComprasService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        estoqueFake.setDisponivel(true);
        estoqueFake.setFalhaNaBaixa(true);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> compraService.finalizarCompra(1L, 1L));

        assertTrue(pagamentoFake.houveCancelamentoChamado(),
                "O sistema deveria ter chamado o cancelamento do pagamento, mas não chamou.");

        assertEquals("Erro ao dar baixa no estoque.", e.getMessage());
    }
}
