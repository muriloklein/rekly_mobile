package com.example.rekly.domain.model

/**
 * Situação da assinatura.
 *
 * - [ATIVA]: entra no total mensal e gera pagamentos presumidos.
 * - [TESTE]: período gratuito; não gera pagamento e aparece separada do total.
 * - [CANCELADA]: mantém o histórico, mas não gera novos pagamentos.
 */
enum class StatusAssinatura {
    ATIVA,
    TESTE,
    CANCELADA
}
