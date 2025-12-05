package ecommerce.util;

import ecommerce.entity.*;

import java.math.BigDecimal;
import java.util.List;

public  class MetodosAuxilar {

    public static Cliente criarCliente(Long id, String nome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setRegiao(Regiao.SUDESTE);
        cliente.setTipo(TipoCliente.OURO);

        return cliente;
    }

    public static ItemCompra criarItem(BigDecimal preco, BigDecimal peso, Long quantidade) {
        Produto produto = new Produto();
        produto.setId(1L);
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
        ItemCompra item = criarItem(preco, peso, quantidade);
        item.getProduto().setFragil(true);
        return item;
    }

    public static CarrinhoDeCompras criarCarrinho(ItemCompra... itensCompras) {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(itensCompras));
        return carrinho;
    }
}
