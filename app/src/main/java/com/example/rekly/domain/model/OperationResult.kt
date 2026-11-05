package com.example.rekly.domain.model

/**
 * Result of an operation that may be blocked by a business rule.
 * The screen turns the [RuleError] into a message (strings.xml).
 */
sealed interface OperationResult<out T> {
    data class Success<out T>(val value: T) : OperationResult<T>
    data class Failure(val error: RuleError) : OperationResult<Nothing>
}
