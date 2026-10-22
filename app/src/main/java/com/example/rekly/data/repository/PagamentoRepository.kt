package com.example.rekly.data.repository

import com.example.rekly.data.entity.Pagamento
import com.example.rekly.data.local.AssinaturaDao
import com.example.rekly.data.local.PagamentoDao
import com.example.rekly.domain.model.ErroRegra
import com.example.rekly.domain.model.Resultado
import com.example.rekly.domain.model.StatusAssinatura
import com.example.rekly.domain.model.StatusPagamento
import com.example.rekly.domain.rules.GeradorPagamentos
import java.time.LocalDate

class PagamentoRepository(
    private val assinaturaDao: AssinaturaDao,
    private val pagamentoDao: PagamentoDao
) {

    /**
     * Gera os pagamentos presumidos de todas as assinaturas ativas do usuário.
     * Chamado ao abrir a MainActivity. Retorna quantos pagamentos foram criados.
     */
    suspend fun gerarPresumidos(usuarioId: Long, hoje: LocalDate = LocalDate.now()): Int {
        val novos = assinaturaDao.listarPorStatus(usuarioId, StatusAssinatura.ATIVA).flatMap { assinatura ->
            GeradorPagamentos.gerar(
                assinatura = assinatura,
                competenciasExistentes = pagamentoDao.listarCompetencias(assinatura.id).toSet(),
                ultimoValorPagoCentavos = pagamentoDao.ultimoValor(assinatura.id, StatusPagamento.PAGO),
                hoje = hoje
            )
        }
        if (novos.isEmpty()) return 0
        return pagamentoDao.inserirTodos(novos).count { it != -1L }
    }

    /** Confirma um pendente com o valor real, que deve ser maior que zero. */
    suspend fun confirmar(
        pagamento: Pagamento,
        valorCentavos: Long,
        hoje: LocalDate = LocalDate.now()
    ): Resultado<Unit> {
        if (valorCentavos <= 0) return Resultado.Falha(ErroRegra.VALOR_INVALIDO)
        pagamentoDao.atualizar(
            pagamento.copy(
                valorCentavos = valorCentavos,
                dataPagamento = hoje,
                status = StatusPagamento.PAGO
            )
        )
        return Resultado.Sucesso(Unit)
    }
}
