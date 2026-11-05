package com.example.rekly.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Expense category. The app's default categories have a null [userId] and [isDefault] true;
 * the ones created by the user belong to them.
 *
 * The name uses collate NOCASE, so "streaming" and "Streaming" are equal in
 * comparisons and in the unique index (per user).
 */
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "name"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val isDefault: Boolean = false,
    val userId: Long? = null
)
