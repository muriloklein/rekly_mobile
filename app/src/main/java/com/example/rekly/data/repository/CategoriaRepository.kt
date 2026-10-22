package com.example.rekly.data.repository

import androidx.lifecycle.LiveData
import com.example.rekly.data.entity.Categoria
import com.example.rekly.data.local.AssinaturaDao
import com.example.rekly.data.local.CategoriaDao
import com.example.rekly.domain.model.ErroRegra
import com.example.rekly.domain.model.Resultado

class CategoriaRepository(
    private val categoriaDao: CategoriaDao,
    private val assinaturaDao: AssinaturaDao
) {

    fun observarDoUsuario(usuarioId: Long): LiveData<List<Categoria>> =
        categoriaDao.observarDoUsuario(usuarioId)

    suspend fun listarDoUsuario(usuarioId: Long): List<Categoria> =
        categoriaDao.listarDoUsuario(usuarioId)

    suspend fun buscarPorId(id: Long): Categoria? = categoriaDao.buscarPorId(id)

    /** Cria categoria pessoal. Nome obrigatório e único entre as padrão e as do usuário. */
    suspend fun criar(usuarioId: Long, nome: String): Resultado<Long> {
        val nomeLimpo = nome.trim()
        validarNome(usuarioId, nomeLimpo, ignorarId = 0)?.let { return Resultado.Falha(it) }
        val id = categoriaDao.inserir(Categoria(nome = nomeLimpo, usuarioId = usuarioId))
        return Resultado.Sucesso(id)
    }

    /** Renomeia categoria pessoal. Padrão não é editável. */
    suspend fun renomear(categoria: Categoria, novoNome: String): Resultado<Unit> {
        val usuarioId = categoria.usuarioId
        if (categoria.isPadrao || usuarioId == null) return Resultado.Falha(ErroRegra.CATEGORIA_PADRAO)
        val nomeLimpo = novoNome.trim()
        validarNome(usuarioId, nomeLimpo, ignorarId = categoria.id)?.let { return Resultado.Falha(it) }
        categoriaDao.atualizar(categoria.copy(nome = nomeLimpo))
        return Resultado.Sucesso(Unit)
    }

    /** Exclui categoria pessoal, bloqueando padrão e categorias com assinaturas. */
    suspend fun excluir(categoria: Categoria): Resultado<Unit> {
        if (categoria.isPadrao) return Resultado.Falha(ErroRegra.CATEGORIA_PADRAO)
        if (assinaturaDao.contarPorCategoria(categoria.id) > 0) {
            return Resultado.Falha(ErroRegra.CATEGORIA_EM_USO)
        }
        categoriaDao.excluir(categoria)
        return Resultado.Sucesso(Unit)
    }

    private suspend fun validarNome(usuarioId: Long, nome: String, ignorarId: Long): ErroRegra? =
        when {
            nome.isEmpty() -> ErroRegra.NOME_OBRIGATORIO
            categoriaDao.contarComNome(usuarioId, nome, ignorarId) > 0 -> ErroRegra.NOME_DUPLICADO
            else -> null
        }
}
