package com.example.rekly.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.entity.Category
import com.example.rekly.data.repository.CategoryRepository
import com.example.rekly.domain.model.OperationResult
import com.example.rekly.domain.model.RuleError
import kotlinx.coroutines.launch

/**
 * Shared by [CategoriesActivity] and [CategoryNameDialog]. Keeping the dialog state here
 * (field error and "saved") lets the dialog survive a rotation without losing it.
 */
class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val userId: Long
) : ViewModel() {

    /** Default categories first, then the user's; the list updates by itself after each change. */
    val categories: LiveData<List<Category>> = categoryRepository.observeByUser(userId)

    private val _nameError = MutableLiveData<RuleError?>()
    val nameError: LiveData<RuleError?> = _nameError

    private val _nameSaved = MutableLiveData(false)
    val nameSaved: LiveData<Boolean> = _nameSaved

    private val _error = MutableLiveData<RuleError?>()
    val error: LiveData<RuleError?> = _error

    /** Creates a category when [categoryId] is null; renames it otherwise. */
    fun saveName(categoryId: Long?, name: String) {
        viewModelScope.launch {
            val result = if (categoryId == null) {
                categoryRepository.create(userId, name)
            } else {
                rename(categoryId, name)
            }
            when (result) {
                is OperationResult.Success -> _nameSaved.value = true
                is OperationResult.Failure -> _nameError.value = result.error
            }
        }
    }

    fun delete(categoryId: Long) {
        viewModelScope.launch {
            val category = findOwn(categoryId)
            val result = if (category == null) {
                OperationResult.Failure(RuleError.NOT_FOUND)
            } else {
                categoryRepository.delete(category)
            }
            if (result is OperationResult.Failure) _error.value = result.error
        }
    }

    /** Called when a new name dialog opens, so it never starts with a leftover error. */
    fun onNameDialogOpened() {
        _nameError.value = null
        _nameSaved.value = false
    }

    fun clearNameError() {
        if (_nameError.value != null) _nameError.value = null
    }

    fun onNameSavedHandled() {
        _nameSaved.value = false
    }

    fun onErrorShown() {
        _error.value = null
    }

    private suspend fun rename(categoryId: Long, name: String): OperationResult<Unit> {
        val category = findOwn(categoryId) ?: return OperationResult.Failure(RuleError.NOT_FOUND)
        return categoryRepository.rename(category, name)
    }

    // Default categories (userId null) are allowed through so the repository explains why they are blocked
    private suspend fun findOwn(categoryId: Long): Category? =
        categoryRepository.findById(categoryId)?.takeIf { it.userId == null || it.userId == userId }

    companion object {
        fun factory(categoryRepository: CategoryRepository, userId: Long) = viewModelFactory {
            initializer { CategoriesViewModel(categoryRepository, userId) }
        }
    }
}
