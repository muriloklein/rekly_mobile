package com.example.rekly.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.rekly.domain.model.StatusPagamento
import java.time.LocalDate
import java.time.YearMonth

/**
 * Pagamento de uma assinatura em uma competência (mês de referência).
 *
 * O índice único por assinatura + competência impede gerar o mesmo mês duas vezes.
 * A chave estrangeira com RESTRICT impede excluir uma assinatura que tenha pagamentos.
 */
@Entity(
    tableName = "pagamentos",
    foreignKeys = [
        ForeignKey(
            entity = Assinatura::class,
            parentColumns = ["id"],
            childColumns = ["assinaturaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["assinaturaId", "competencia"], unique = true)]
)
data class Pagamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assinaturaId: Long,
    val competencia: YearMonth,
    /** Valor pago ou, se pendente, estimativa (último valor pago). */
    val valorCentavos: Long,
    val dataVencimento: LocalDate,
    /** Nulo enquanto pendente. */
    val dataPagamento: LocalDate? = null,
    val status: StatusPagamento
)
