package com.example.rekly.domain.model

/**
 * Resultado de uma operação que pode ser barrada por regra de negócio.
 * A tela transforma o [ErroRegra] em mensagem (strings.xml).
 */
sealed interface Resultado<out T> {
    data class Sucesso<out T>(val valor: T) : Resultado<T>
    data class Falha(val erro: ErroRegra) : Resultado<Nothing>
}
