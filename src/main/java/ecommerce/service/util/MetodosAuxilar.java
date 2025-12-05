package ecommerce.service.util;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;

import java.math.BigDecimal;
import java.util.List;

public  class MetodosAuxilar {

    public static ItemCompra criarItem(BigDecimal preco, BigDecimal peso, Long quantidade) {
        Produto produto = new Produto();
        produto.setPreco(preco);
        produto.setPesoFisico(peso);
        produto.setTipo(TipoProduto.ELETRONICO);
        produto.setFragil(false);
        produto.setNome("Produto de teste");

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        return item;
    }

    public static ItemCompra criarItemFragil(BigDecimal preco, BigDecimal peso, Long quantidade) {
        ItemCompra item = new ItemCompra();
        item.getProduto().setFragil(true);
        return item;
    }

    public static CarrinhoDeCompras criarCarrinho(ItemCompra... itensCompras) {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(itensCompras));
        return carrinho;
    }
}
