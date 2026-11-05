package com.example.rekly.domain.rules

import com.example.rekly.domain.model.ErroRegra

object Validadores {

    private const val TAMANHO_MINIMO_NOME = 3
    private const val TAMANHO_MINIMO_SENHA = 6
    private val formatoEmail = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validarNome(nome: String): ErroRegra? = when {
        nome.isBlank() -> ErroRegra.NOME_OBRIGATORIO
        nome.trim().length < TAMANHO_MINIMO_NOME -> ErroRegra.NOME_CURTO
        else -> null
    }

    fun validarEmail(email: String): ErroRegra? =
        if (formatoEmail.matches(email.trim())) null else ErroRegra.EMAIL_INVALIDO

    fun validarSenhaPreenchida(senha: String): ErroRegra? =
        if (senha.isEmpty()) ErroRegra.SENHA_OBRIGATORIA else null

    fun validarSenhaNova(senha: String): ErroRegra? =
        if (senha.length < TAMANHO_MINIMO_SENHA) ErroRegra.SENHA_CURTA else null

    fun validarConfirmacao(senha: String, confirmacao: String): ErroRegra? =
        if (senha == confirmacao) null else ErroRegra.SENHAS_DIFERENTES
}
