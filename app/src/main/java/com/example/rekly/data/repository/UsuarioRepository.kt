package com.example.rekly.data.repository

import com.example.rekly.data.entity.Usuario
import com.example.rekly.data.local.UsuarioDao
import com.example.rekly.domain.model.ErroRegra
import com.example.rekly.domain.model.Resultado
import com.example.rekly.domain.rules.HashSenha
import java.time.LocalDate

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        hoje: LocalDate = LocalDate.now()
    ): Resultado<Long> {
        val emailNormalizado = normalizarEmail(email)
        if (usuarioDao.buscarPorEmail(emailNormalizado) != null) {
            return Resultado.Falha(ErroRegra.EMAIL_JA_CADASTRADO)
        }
        val salt = HashSenha.gerarSalt()
        val id = usuarioDao.inserir(
            Usuario(
                nome = nome.trim(),
                email = emailNormalizado,
                senhaHash = HashSenha.calcular(senha, salt),
                salt = salt,
                criadoEm = hoje
            )
        )
        return Resultado.Sucesso(id)
    }

    suspend fun autenticar(email: String, senha: String): Resultado<Usuario> {
        val usuario = usuarioDao.buscarPorEmail(normalizarEmail(email))
        if (usuario == null || !HashSenha.confere(senha, usuario.salt, usuario.senhaHash)) {
            return Resultado.Falha(ErroRegra.CREDENCIAIS_INVALIDAS)
        }
        return Resultado.Sucesso(usuario)
    }

    private fun normalizarEmail(email: String): String = email.trim().lowercase()
}
