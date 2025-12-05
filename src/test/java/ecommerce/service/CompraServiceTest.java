package ecommerce.service;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static ecommerce.service.util.MetodosAuxilar.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CompraServiceTest {

    @Mock
    CarrinhoDeComprasService carrinhoDeComprasService;

    @Mock
    ClienteService clienteService;

    @Mock
    IEstoqueExternal estoqueExternal;

    @Mock
    IPagamentoExternal pagamentoExternal;

    @InjectMocks
    private CompraService compraService;

    // Testes de Desconto (Usamos nested para visualizar melhor os testes)
    @Nested
    @DisplayName("Regras de Desconto por Valor Total")
    class DescontoTests {
        // Dessa vez não estamos usando arquivos .csv para os testes parametrizados
        @ParameterizedTest(name = "Compra de R$ {0} deve resultar em total de R$ {1}")
        @MethodSource("cenariosDeDesconto")
        void deveCalcularCorretamente(BigDecimal valorCompra, BigDecimal totalEsperado) {
            ItemCompra itemCompra = criarItem(valorCompra, new BigDecimal("1.00"), 1L);
            CarrinhoDeCompras carrinho = criarCarrinho(itemCompra);

            BigDecimal resultado = compraService.calcularCustoTotal(carrinho);

            assertEquals(resultado, totalEsperado);
        }

        private static Stream<Arguments> cenariosDeDesconto() {
            return Stream.of(
                // Partição 1: Sem desconto pois o valor da compra é menor que 500
                Arguments.of(new BigDecimal("499.99"), new BigDecimal("499.99")),

                // Partição 2: Com desconto de 10% pois o valor é maior ou igual a 500 e menor que 1000.00
                Arguments.of(new BigDecimal("500.00"), new BigDecimal("450.00")),
                Arguments.of(new BigDecimal("500.01"), new BigDecimal("450.01")),
                Arguments.of(new BigDecimal("999.99"), new BigDecimal("899.99")),

                // Partição 3: Com desconto de 20% pois o valor é maior ou igual a 1000.00
                Arguments.of(new BigDecimal("1000.00"), new BigDecimal("800.00")),
                Arguments.of(new BigDecimal("1000.01"), new BigDecimal("800.01"))
            );
        }
    }
}
