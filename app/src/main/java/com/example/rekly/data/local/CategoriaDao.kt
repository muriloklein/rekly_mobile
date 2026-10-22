package com.example.rekly.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.rekly.data.entity.Categoria

@Dao
interface CategoriaDao {

    @Insert
    suspend fun inserir(categoria: Categoria): Long

    @Update
    suspend fun atualizar(categoria: Categoria)

    @Delete
    suspend fun excluir(categoria: Categoria)

    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun buscarPorId(id: Long): Categoria?

    /** Padrão primeiro, depois as do usuário, em ordem alfabética. */
    @Query(
        "SELECT * FROM categorias WHERE usuarioId IS NULL OR usuarioId = :usuarioId " +
            "ORDER BY isPadrao DESC, nome"
    )
    fun observarDoUsuario(usuarioId: Long): LiveData<List<Categoria>>

    @Query(
        "SELECT * FROM categorias WHERE usuarioId IS NULL OR usuarioId = :usuarioId " +
            "ORDER BY isPadrao DESC, nome"
    )
    suspend fun listarDoUsuario(usuarioId: Long): List<Categoria>

    /**
     * Quantas categorias visíveis ao usuário (padrão + dele) já usam [nome],
     * ignorando a de id [ignorarId] (use 0 ao criar). Sem diferenciar maiúsculas.
     */
    @Query(
        "SELECT COUNT(*) FROM categorias " +
            "WHERE (usuarioId IS NULL OR usuarioId = :usuarioId) AND nome = :nome AND id != :ignorarId"
    )
    suspend fun contarComNome(usuarioId: Long, nome: String, ignorarId: Long): Int
}
