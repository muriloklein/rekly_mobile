package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Assinatura
import com.example.rekly.domain.model.Periodo
import com.example.rekly.domain.model.StatusAssinatura
import java.time.LocalDate
import java.time.YearMonth

/**
 * Cálculos de gastos portados da versão web. Funções puras, todas em centavos,
 * com arredondamento "meio para cima" (0,5 centavo vira 1).
 */
object CalculoGastos {

    /** Mensalidade = valor ÷ meses do período. */
    fun mensalidadeCentavos(valorCentavos: Long, periodo: Periodo): Long =
        dividirArredondando(valorCentavos, periodo.meses.toLong())

    /** Custo real = mensalidade ÷ participantes (mínimo 1), calculado de uma vez para arredondar só no fim. */
    fun custoRealCentavos(valorCentavos: Long, periodo: Periodo, participantes: Int): Long =
        dividirArredondando(valorCentavos, periodo.meses.toLong() * participantes.coerceAtLeast(1))

    /** Data de vencimento na competência; dia 31 em mês curto cai no último dia do mês. */
    fun vencimentoNaCompetencia(competencia: YearMonth, diaCobranca: Int): LocalDate =
        competencia.atDay(diaCobranca.coerceIn(1, competencia.lengthOfMonth()))

    /**
     * Sequência (infinita e crescente) de competências com vencimento: começa no mês
     * da data de início e avança de [Periodo.meses] em [Periodo.meses] meses. Um
     * vencimento anterior à data de início é descartado.
     */
    fun vencimentos(
        dataInicio: LocalDate,
        periodo: Periodo,
        diaCobranca: Int
    ): Sequence<Pair<YearMonth, LocalDate>> =
        generateSequence(YearMonth.from(dataInicio)) { it.plusMonths(periodo.meses.toLong()) }
            .map { competencia -> competencia to vencimentoNaCompetencia(competencia, diaCobranca) }
            .filter { (_, vencimento) -> !vencimento.isBefore(dataInicio) }

    /** Próximo vencimento a partir de [hoje], inclusive (vence hoje conta como próximo). */
    fun proximoVencimento(
        dataInicio: LocalDate,
        periodo: Periodo,
        diaCobranca: Int,
        hoje: LocalDate
    ): LocalDate =
        vencimentos(dataInicio, periodo, diaCobranca)
            .map { (_, vencimento) -> vencimento }
            .first { !it.isBefore(hoje) }

    // Versões que recebem a assinatura; devolvem null se ela ainda estiver incompleta.

    fun mensalidadeCentavos(assinatura: Assinatura): Long? =
        assinatura.valorCentavos?.let { mensalidadeCentavos(it, assinatura.periodo) }

    fun custoRealCentavos(assinatura: Assinatura): Long? =
        assinatura.valorCentavos?.let { custoRealCentavos(it, assinatura.periodo, assinatura.participantes) }

    /** Null se incompleta ou cancelada. */
    fun proximoVencimento(assinatura: Assinatura, hoje: LocalDate): LocalDate? {
        val dataInicio = assinatura.dataInicio ?: return null
        val diaCobranca = assinatura.diaCobranca ?: return null
        if (assinatura.status == StatusAssinatura.CANCELADA) return null
        return proximoVencimento(dataInicio, assinatura.periodo, diaCobranca, hoje)
    }

    /** Soma das mensalidades das assinaturas ATIVAS e completas (teste fica de fora). */
    fun totalMensalCentavos(assinaturas: List<Assinatura>): Long =
        assinaturas.filter { it.status == StatusAssinatura.ATIVA }
            .sumOf { mensalidadeCentavos(it) ?: 0L }

    /** Soma dos custos reais (após rateio) das assinaturas ATIVAS e completas. */
    fun totalCustoRealCentavos(assinaturas: List<Assinatura>): Long =
        assinaturas.filter { it.status == StatusAssinatura.ATIVA }
            .sumOf { custoRealCentavos(it) ?: 0L }

    /** Divisão inteira arredondando meio para cima (valores não negativos). */
    private fun dividirArredondando(valor: Long, divisor: Long): Long =
        (valor * 2 + divisor) / (divisor * 2)
}
