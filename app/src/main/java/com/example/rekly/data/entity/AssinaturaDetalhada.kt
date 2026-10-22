package com.example.rekly.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Assinatura com a categoria (N:1) e o histórico de pagamentos (1:N), usada no detalhe.
 * A ordem de [pagamentos] não é garantida pelo Room; ordene na tela por competência.
 */
data class AssinaturaDetalhada(
    @Embedded val assinatura: Assinatura,
    @Relation(parentColumn = "categoriaId", entityColumn = "id")
    val categoria: Categoria,
    @Relation(parentColumn = "id", entityColumn = "assinaturaId")
    val pagamentos: List<Pagamento>
)
