package com.example.rekly.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The app's default categories, with fixed ids so the service catalog can
 * reference them directly.
 *
 * They are inserted in the database's onOpen with INSERT OR IGNORE: this way they
 * show up both in new installs and in databases that arrived through AutoMigration
 * (where onCreate does not run), without duplicates.
 */
object DefaultCategories {

    const val STREAMING = 1L
    const val MUSIC = 2L
    const val HOUSING = 3L
    const val TELECOM = 4L
    const val HEALTH_WELLNESS = 5L
    const val INSURANCE = 6L
    const val OTHER = 7L

    val all: Map<Long, String> = linkedMapOf(
        STREAMING to "Streaming",
        MUSIC to "Música",
        HOUSING to "Moradia",
        TELECOM to "Telecom",
        HEALTH_WELLNESS to "Saúde e bem-estar",
        INSURANCE to "Seguros",
        OTHER to "Outros"
    )

    fun insertMissing(db: SupportSQLiteDatabase) {
        all.forEach { (id, name) ->
            db.execSQL(
                "INSERT OR IGNORE INTO categories (id, name, isDefault, userId) VALUES (?, ?, 1, NULL)",
                arrayOf<Any>(id, name)
            )
        }
    }
}
