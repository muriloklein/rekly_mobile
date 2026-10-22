package com.example.rekly.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.rekly.data.entity.Assinatura
import com.example.rekly.data.entity.AssinaturaComCategoria
import com.example.rekly.data.entity.AssinaturaDetalhada
import com.example.rekly.domain.model.StatusAssinatura

@Dao
interface AssinaturaDao {

    @Insert
    suspend fun inserir(assinatura: Assinatura): Long

    /** Criação em lote do onboarding. */
    @Insert
    suspend fun inserirTodas(assinaturas: List<Assinatura>): List<Long>

    @Update
    suspend fun atualizar(assinatura: Assinatura)

    @Delete
    suspend fun excluir(assinatura: Assinatura)

    @Query("SELECT * FROM assinaturas WHERE id = :id")
    suspend fun buscarPorId(id: Long): Assinatura?

    @Transaction
    @Query("SELECT * FROM assinaturas WHERE usuarioId = :usuarioId ORDER BY nomeServico COLLATE NOCASE")
    fun observarComCategoria(usuarioId: Long): LiveData<List<AssinaturaComCategoria>>

    @Transaction
    @Query("SELECT * FROM assinaturas WHERE id = :id")
    fun observarDetalhada(id: Long): LiveData<AssinaturaDetalhada?>

    @Query("SELECT * FROM assinaturas WHERE usuarioId = :usuarioId AND status = :status")
    suspend fun listarPorStatus(usuarioId: Long, status: StatusAssinatura): List<Assinatura>

    @Query("SELECT COUNT(*) FROM assinaturas WHERE categoriaId = :categoriaId")
    suspend fun contarPorCategoria(categoriaId: Long): Int
}
