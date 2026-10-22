package com.example.rekly.domain.model

/**
 * - [PAGO]: valor fixo (gerado automaticamente) ou valor variável já confirmado.
 * - [PENDENTE]: valor variável aguardando o usuário confirmar o valor real.
 */
enum class StatusPagamento {
    PAGO,
    PENDENTE
}
