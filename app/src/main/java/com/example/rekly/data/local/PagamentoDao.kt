package com.example.rekly.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rekly.data.entity.Pagamento
import com.example.rekly.domain.model.StatusPagamento
import java.time.YearMonth

@Dao
interface PagamentoDao {

    /**
     * Insere ignorando competências que já existem (índice único), o que torna a
     * geração de pagamentos segura mesmo se rodar duas vezes ao mesmo tempo.
     * Retorna -1 para cada linha ignorada.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirTodos(pagamentos: List<Pagamento>): List<Long>

    @Update
    suspend fun atualizar(pagamento: Pagamento)

    @Query("SELECT * FROM pagamentos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Pagamento?

    @Query("SELECT competencia FROM pagamentos WHERE assinaturaId = :assinaturaId")
    suspend fun listarCompetencias(assinaturaId: Long): List<YearMonth>

    @Query(
        "SELECT valorCentavos FROM pagamentos WHERE assinaturaId = :assinaturaId AND status = :status " +
            "ORDER BY competencia DESC LIMIT 1"
    )
    suspend fun ultimoValor(assinaturaId: Long, status: StatusPagamento): Long?

    @Query("SELECT COUNT(*) FROM pagamentos WHERE assinaturaId = :assinaturaId")
    suspend fun contarPorAssinatura(assinaturaId: Long): Int
}
