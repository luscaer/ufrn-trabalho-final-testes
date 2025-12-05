package ecommerce.service;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static ecommerce.util.MetodosAuxilar.*;
import static org.junit.jupiter.api.Assertions.*;

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

            assertEquals(totalEsperado, resultado);
        }

        private static Stream<Arguments> cenariosDeDesconto() {
            return Stream.of(
                    // Partição 1: Sem desconto pois o valor da compra é menor que 500
                    Arguments.of(new BigDecimal("0.00"), new BigDecimal("0.00")),
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

    // Testes de Frete
    @Nested
    @DisplayName("Regras de frete")
    class FreteTests {

        @ParameterizedTest(name = "Peso {0}kg deve gerar custo total de R$ {1} (Frete incluso)")
        @MethodSource("cenariosDeFrete")
        void deveCalcularFreteBaseCorretamente(BigDecimal peso, BigDecimal custoTotalEsperado) {
            ItemCompra item = criarItem(new BigDecimal("10.00"), peso, 1L);
            CarrinhoDeCompras carrinho = criarCarrinho(item);

            BigDecimal resultado = compraService.calcularCustoTotal(carrinho);

            assertEquals(custoTotalEsperado, resultado);
        }

        private static Stream<Arguments> cenariosDeFrete() {
            return Stream.of(
                    // Partição 1: Sem custo de frete pois peso menor ou igual a 5
                    Arguments.of(new BigDecimal("4.99"), new BigDecimal("10.00")),
                    Arguments.of(new BigDecimal("5.00"), new BigDecimal("10.00")),

                    // Partição 2: Custo de frete de R$ 2.00 por quilo, pois peso maior que 5 e menor ou igual a 10
                    Arguments.of(new BigDecimal("5.01"), new BigDecimal("20.02")),
                    Arguments.of(new BigDecimal("9.99"), new BigDecimal("29.98")),
                    Arguments.of(new BigDecimal("10.00"), new BigDecimal("30.00")),

                    // Partição 3: Custo de frete de R$ 4.00 por quilo, pois peso maior que 10 e menor ou igual a 50
                    Arguments.of(new BigDecimal("10.01"), new BigDecimal("50.04")),
                    Arguments.of(new BigDecimal("49.99"), new BigDecimal("209.96")),
                    Arguments.of(new BigDecimal("50.00"), new BigDecimal("210.00")),

                    // Partição 4: Custo de frete de R$ 7.00, pois peso maior que 50
                    Arguments.of(new BigDecimal("50.01"), new BigDecimal("360.07"))
            );
        }

        @Test
        @DisplayName("Deve somar taxa de fragilidade de R$ 5,00 por item")
        void deveSomarTaxaDeFragilidad() {
            ItemCompra item = criarItemFragil(new BigDecimal("10.00"), new BigDecimal("1.00"), 1L);
            CarrinhoDeCompras carrinho = criarCarrinho(item);

            BigDecimal resultado = compraService.calcularCustoTotal(carrinho);

            assertEquals(new BigDecimal("15.00"), resultado);
        }

        @Test
        @DisplayName("Deve somar taxa de fragiliade de R$ 5.00 multiplicada pela quantidade")
        void deveSomarTaxaDeFragilidadPorQuantidade() {
            ItemCompra item = criarItemFragil(new BigDecimal("10.00"), new BigDecimal("1.00"), 3L);
            CarrinhoDeCompras carrinho = criarCarrinho(item);

            BigDecimal resultado = compraService.calcularCustoTotal(carrinho);

            assertEquals(new BigDecimal("45.00"), resultado);
        }
    }

    // Testes de Robustez
    @Nested
    @DisplayName("Validações de Entrada")
    class ValidacaoTests {

        @Test
        @DisplayName("Deve lançar exceção se carrinho for nulo")
        void deveLancarExcecaoCarrinhoNulo() {
            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(null));
        }

        @Test
        @DisplayName("Deve lançar exceção se a lista de itens do carrinho for nula")
        void deveLancarExcecaoCarrinhoListaDeItensNula() {
            CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
            carrinho.setItens(null);

            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(carrinho));
        }

        @Test
        @DisplayName("Deve lançar exceção se carrinho estiver vazio")
        void deveLancarExcecaoCarrinhoVazio() {
            CarrinhoDeCompras carrinho = criarCarrinho();
            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(carrinho));
        }

        @Test
        @DisplayName("Deve lançar exceção se item for nulo")
        void deveLancarExcecaoItemNulo() {
            CarrinhoDeCompras carrinho = new CarrinhoDeCompras();

            List<ItemCompra> itens = new ArrayList<>();
            itens.add(null);

            carrinho.setItens(itens);

            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(carrinho));
        }

        @Test
        @DisplayName("Deve lançar exceção se produto for nulo")
        void deveLancarExcecaoProdutoNulo() {
            ItemCompra item = new ItemCompra();
            item.setProduto(null);

            CarrinhoDeCompras carrinho = criarCarrinho(item);

            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(carrinho));
        }

        // Pesquisando achei esse @NullSource que já implementa o caso nulo em testes parametrizados
        @ParameterizedTest(name = "Quantidade inválida {0} deve lançar exceção")
        @MethodSource("quantidadeInvalidaDeItens")
        @NullSource
        void deveLancarExcecaoQuantidadeInvalida(Long quantidade) {
            ItemCompra item = criarItem(new BigDecimal("10.00"), new BigDecimal("1.00"), quantidade);

            CarrinhoDeCompras carrinho = criarCarrinho(item);

            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(carrinho));
        }

        private static Stream<Arguments> quantidadeInvalidaDeItens() {
            return Stream.of(
                    // Limites 0 e 1 para quantidades inválidas
                    Arguments.of(0L),
                    Arguments.of(-1L),
                    //Partição negativa comum
                    Arguments.of(-10L)
            );
        }

        @ParameterizedTest(name = "Preço inválido {0} deve lançar exceção")
        @MethodSource("precosInvalidos")
        @NullSource
        void deveLancarExcecaoPrecoInvalido(BigDecimal preco) {
            ItemCompra item = criarItem(preco, new BigDecimal("1.00"), 1L);

            CarrinhoDeCompras carrinho = criarCarrinho(item);

            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(carrinho));
        }

        private static Stream<Arguments> precosInvalidos() {
            return Stream.of(
                    // Limite negativo para preços
                    Arguments.of(new BigDecimal("-0.01")),
                    //Partição negativa comum
                    Arguments.of(new BigDecimal("-10.00"))
            );
        }

        @Test
        @DisplayName("Deve lançar exceção se tipo do produto for nulo")
        void deveLancarExcecaoTipoProdutoNulo() {
            ItemCompra item = criarItem(new BigDecimal("10.00"), new BigDecimal("1.00"), 1L);
            item.getProduto().setTipo(null);

            CarrinhoDeCompras carrinho = criarCarrinho(item);

            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(carrinho));
        }

        @ParameterizedTest(name = "Peso inválido {0} deve lançar exceção")
        @MethodSource("pesosInvalidos")
        @NullSource
        void deveLancarExcecaoPesoInvalido(BigDecimal peso) {
            ItemCompra item = criarItem(new BigDecimal("10.00"), peso, 1L);

            CarrinhoDeCompras carrinho = criarCarrinho(item);

            assertThrows(IllegalArgumentException.class, () -> compraService.calcularCustoTotal(carrinho));
        }

        private static Stream<Arguments> pesosInvalidos() {
            return Stream.of(
                    // Valores limites para peso
                    Arguments.of(BigDecimal.ZERO),
                    Arguments.of(new BigDecimal("-0.01")),
                    //Partição negativa comum
                    Arguments.of(new BigDecimal("-10.00"))
            );
        }
    }
}
