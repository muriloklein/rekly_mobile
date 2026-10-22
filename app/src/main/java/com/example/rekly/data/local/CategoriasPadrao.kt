package com.example.rekly.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Categorias padrão do app, com ids fixos para o catálogo de serviços poder
 * referenciá-las direto.
 *
 * São inseridas no onOpen do banco com INSERT OR IGNORE: assim aparecem tanto
 * em instalações novas quanto em bancos que chegaram por AutoMigration (onde o
 * onCreate não roda), sem duplicar.
 */
object CategoriasPadrao {

    const val STREAMING = 1L
    const val MUSICA = 2L
    const val MORADIA = 3L
    const val TELECOM = 4L
    const val SAUDE_BEM_ESTAR = 5L
    const val SEGUROS = 6L
    const val OUTROS = 7L

    val todas: Map<Long, String> = linkedMapOf(
        STREAMING to "Streaming",
        MUSICA to "Música",
        MORADIA to "Moradia",
        TELECOM to "Telecom",
        SAUDE_BEM_ESTAR to "Saúde e bem-estar",
        SEGUROS to "Seguros",
        OUTROS to "Outros"
    )

    fun inserirSeFaltar(db: SupportSQLiteDatabase) {
        todas.forEach { (id, nome) ->
            db.execSQL(
                "INSERT OR IGNORE INTO categorias (id, nome, isPadrao, usuarioId) VALUES (?, ?, 1, NULL)",
                arrayOf<Any>(id, nome)
            )
        }
    }
}
