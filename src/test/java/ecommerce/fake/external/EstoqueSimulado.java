package ecommerce.fake.external;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.external.IEstoqueExternal;

@Service
public class EstoqueSimulado implements IEstoqueExternal {

    private boolean disponivel;
    private boolean falhaNaBaixa;

    private List<Long> idsRecebidos;
    private List<Long> qntdsRecebidas;

    public List<Long> getIdsRecebidos() { return idsRecebidos; }
    public List<Long> getQntdsRecebidas() { return qntdsRecebidas; }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public void setFalhaNaBaixa(boolean falha) {
        this.falhaNaBaixa = falha;
    }

	@Override
	public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades)
	{
		return new EstoqueBaixaDTO(!falhaNaBaixa);
	}

	@Override
	public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades)
	{
        this.idsRecebidos = produtosIds;
        this.qntdsRecebidas = produtosQuantidades;

		return new DisponibilidadeDTO(disponivel, Collections.emptyList());
	}
}
