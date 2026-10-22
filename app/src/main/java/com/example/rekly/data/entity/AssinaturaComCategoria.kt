package com.example.rekly.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/** Assinatura junto com a categoria (relação N:1), usada na lista. */
data class AssinaturaComCategoria(
    @Embedded val assinatura: Assinatura,
    @Relation(parentColumn = "categoriaId", entityColumn = "id")
    val categoria: Categoria
)
