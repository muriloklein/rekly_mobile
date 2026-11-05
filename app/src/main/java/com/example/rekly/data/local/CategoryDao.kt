package com.example.rekly.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.rekly.data.entity.Category

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun findById(id: Long): Category?

    /** Default ones first, then the user's, in alphabetical order. */
    @Query(
        "SELECT * FROM categories WHERE userId IS NULL OR userId = :userId " +
            "ORDER BY isDefault DESC, name"
    )
    fun observeByUser(userId: Long): LiveData<List<Category>>

    @Query(
        "SELECT * FROM categories WHERE userId IS NULL OR userId = :userId " +
            "ORDER BY isDefault DESC, name"
    )
    suspend fun listByUser(userId: Long): List<Category>

    /**
     * How many categories visible to the user (default + theirs) already use [name],
     * ignoring the one with id [ignoreId] (use 0 when creating). Case-insensitive.
     */
    @Query(
        "SELECT COUNT(*) FROM categories " +
            "WHERE (userId IS NULL OR userId = :userId) AND name = :name AND id != :ignoreId"
    )
    suspend fun countWithName(userId: Long, name: String, ignoreId: Long): Int
}
