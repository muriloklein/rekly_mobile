package com.example.rekly.domain.model

/**
 * Periodicidade da cobrança. [meses] é usado para converter o valor cobrado
 * em mensalidade e para saber de quantos em quantos meses há vencimento.
 */
enum class Periodo(val meses: Int) {
    MENSAL(1),
    TRIMESTRAL(3),
    SEMESTRAL(6),
    ANUAL(12)
}
