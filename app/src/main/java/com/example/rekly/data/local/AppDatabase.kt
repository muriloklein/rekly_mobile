package com.example.rekly.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rekly.data.entity.Assinatura
import com.example.rekly.data.entity.Categoria
import com.example.rekly.data.entity.Pagamento
import com.example.rekly.data.entity.Usuario

@Database(
    entities = [Usuario::class, Categoria::class, Assinatura::class, Pagamento::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun assinaturaDao(): AssinaturaDao
    abstract fun pagamentoDao(): PagamentoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rekly.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        // onOpen (e não onCreate) para cobrir também bancos migrados
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CategoriasPadrao.inserirSeFaltar(db)
                        }
                    })
                    .build()
                    .also { instancia = it }
            }
    }
}
