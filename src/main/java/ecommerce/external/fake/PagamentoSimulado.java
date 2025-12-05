package ecommerce.external.fake;

import org.springframework.stereotype.Service;

import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

@Service
public class PagamentoSimulado implements IPagamentoExternal {

    private boolean autorizado = true;
    private boolean cancelamentoChamado = false;

    public boolean houveCancelamentoChamado() {
        return this.cancelamentoChamado;
    }

    public void setAutorizado(boolean autorizado) {
        this.autorizado = autorizado;
    }

    @Override
	public PagamentoDTO autorizarPagamento(Long clienteId, Double custoTotal)
	{
		return new PagamentoDTO(autorizado, autorizado ? 999L : null);
	}

	@Override
	public void cancelarPagamento(Long clienteId, Long pagamentoTransacaoId)
	{
        this.cancelamentoChamado = true;
	}
}
