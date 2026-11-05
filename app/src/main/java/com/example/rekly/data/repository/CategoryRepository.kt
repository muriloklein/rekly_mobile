package com.example.rekly.data.repository

import androidx.lifecycle.LiveData
import com.example.rekly.data.entity.Category
import com.example.rekly.data.local.CategoryDao
import com.example.rekly.data.local.SubscriptionDao
import com.example.rekly.domain.model.OperationResult
import com.example.rekly.domain.model.RuleError

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val subscriptionDao: SubscriptionDao
) {

    fun observeByUser(userId: Long): LiveData<List<Category>> =
        categoryDao.observeByUser(userId)

    suspend fun listByUser(userId: Long): List<Category> =
        categoryDao.listByUser(userId)

    suspend fun findById(id: Long): Category? = categoryDao.findById(id)

    /** Creates a personal category. Name required and unique among the default ones and the user's. */
    suspend fun create(userId: Long, name: String): OperationResult<Long> {
        val cleanName = name.trim()
        validateName(userId, cleanName, ignoreId = 0)?.let { return OperationResult.Failure(it) }
        val id = categoryDao.insert(Category(name = cleanName, userId = userId))
        return OperationResult.Success(id)
    }

    /** Renames a personal category. Default ones cannot be edited. */
    suspend fun rename(category: Category, newName: String): OperationResult<Unit> {
        val userId = category.userId
        if (category.isDefault || userId == null) return OperationResult.Failure(RuleError.DEFAULT_CATEGORY)
        val cleanName = newName.trim()
        validateName(userId, cleanName, ignoreId = category.id)?.let { return OperationResult.Failure(it) }
        categoryDao.update(category.copy(name = cleanName))
        return OperationResult.Success(Unit)
    }

    /** Deletes a personal category, blocking default ones and categories with subscriptions. */
    suspend fun delete(category: Category): OperationResult<Unit> {
        if (category.isDefault) return OperationResult.Failure(RuleError.DEFAULT_CATEGORY)
        if (subscriptionDao.countByCategory(category.id) > 0) {
            return OperationResult.Failure(RuleError.CATEGORY_IN_USE)
        }
        categoryDao.delete(category)
        return OperationResult.Success(Unit)
    }

    private suspend fun validateName(userId: Long, name: String, ignoreId: Long): RuleError? =
        when {
            name.isEmpty() -> RuleError.NAME_REQUIRED
            categoryDao.countWithName(userId, name, ignoreId) > 0 -> RuleError.DUPLICATE_NAME
            else -> null
        }
}
