package com.example.rekly.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Categoria de gasto. As padrão do app têm [usuarioId] nulo e [isPadrao] verdadeiro;
 * as criadas pelo usuário pertencem a ele.
 *
 * O nome usa collate NOCASE, então "streaming" e "Streaming" são iguais nas
 * comparações e no índice único (por usuário).
 */
@Entity(
    tableName = "categorias",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId", "nome"], unique = true)]
)
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val nome: String,
    val isPadrao: Boolean = false,
    val usuarioId: Long? = null
)
