package ecommerce.service;

import ecommerce.dto.DisponibilidadeDTO;
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

import static ecommerce.util.MetodosAuxilar.*;
import static org.mockito.ArgumentMatchers.*;
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
    }
}
