package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Assinatura
import com.example.rekly.data.entity.Pagamento
import com.example.rekly.domain.model.StatusAssinatura
import com.example.rekly.domain.model.StatusPagamento
import java.time.LocalDate
import java.time.YearMonth

/**
 * Pagamento presumido: ao abrir o app, cria os pagamentos das competências cujo
 * vencimento já chegou e que ainda não existem.
 */
object GeradorPagamentos {

    /**
     * Pagamentos que faltam para [assinatura] até [hoje].
     *
     * Regras:
     * - Só assinaturas ATIVAS e completas (teste e cancelada não geram).
     * - Competências do ciclo do período (anual gera 1 por ano, não 12).
     * - Vencimento até hoje, inclusive.
     * - Só a partir do mês de [Assinatura.ativaDesde], sem pagamentos retroativos.
     * - Pula as competências em [competenciasExistentes].
     * - Valor fixo entra PAGO com o valor da assinatura, pago no vencimento.
     * - Valor variável entra PENDENTE com o último valor pago como estimativa
     *   (ou o valor da assinatura, se ainda não houver pagamento).
     */
    fun gerar(
        assinatura: Assinatura,
        competenciasExistentes: Set<YearMonth>,
        ultimoValorPagoCentavos: Long?,
        hoje: LocalDate
    ): List<Pagamento> {
        if (assinatura.status != StatusAssinatura.ATIVA) return emptyList()
        val valor = assinatura.valorCentavos ?: return emptyList()
        val dataInicio = assinatura.dataInicio ?: return emptyList()
        val diaCobranca = assinatura.diaCobranca ?: return emptyList()
        val inicioGeracao = YearMonth.from(assinatura.ativaDesde ?: return emptyList())

        return CalculoGastos.vencimentos(dataInicio, assinatura.periodo, diaCobranca)
            .takeWhile { (_, vencimento) -> !vencimento.isAfter(hoje) }
            .filter { (competencia, _) -> competencia >= inicioGeracao && competencia !in competenciasExistentes }
            .map { (competencia, vencimento) ->
                if (assinatura.valorVariavel) {
                    Pagamento(
                        assinaturaId = assinatura.id,
                        competencia = competencia,
                        valorCentavos = ultimoValorPagoCentavos ?: valor,
                        dataVencimento = vencimento,
                        dataPagamento = null,
                        status = StatusPagamento.PENDENTE
                    )
                } else {
                    Pagamento(
                        assinaturaId = assinatura.id,
                        competencia = competencia,
                        valorCentavos = valor,
                        dataVencimento = vencimento,
                        dataPagamento = vencimento,
                        status = StatusPagamento.PAGO
                    )
                }
            }
            .toList()
    }

    /**
     * Ajusta [Assinatura.ativaDesde] ao salvar: marca [hoje] quando a assinatura passa
     * a ser ATIVA (criação, teste convertido, cancelada reativada) e mantém a data
     * anterior nos demais casos. [anterior] é null na criação.
     */
    fun ajustarAtivacao(anterior: Assinatura?, nova: Assinatura, hoje: LocalDate): Assinatura {
        val passouAAtiva = nova.status == StatusAssinatura.ATIVA &&
            (anterior == null || anterior.status != StatusAssinatura.ATIVA)
        val ativaDesde = when {
            passouAAtiva -> hoje
            nova.status == StatusAssinatura.ATIVA -> anterior?.ativaDesde ?: hoje
            else -> anterior?.ativaDesde
        }
        return nova.copy(ativaDesde = ativaDesde)
    }
}
