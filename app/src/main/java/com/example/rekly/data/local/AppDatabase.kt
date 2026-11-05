package com.example.rekly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rekly.data.entity.Category
import com.example.rekly.data.entity.Payment
import com.example.rekly.data.entity.Subscription
import com.example.rekly.data.entity.User

@Database(
    entities = [User::class, Category::class, Subscription::class, Payment::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rekly.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        // onOpen (not onCreate) so databases that arrived through migration are covered too
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            DefaultCategories.insertMissing(db)
                        }
                    })
                    .build()
                    .also { instance = it }
            }
    }
}
