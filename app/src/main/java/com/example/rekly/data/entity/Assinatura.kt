package com.example.rekly.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.rekly.domain.model.Periodo
import com.example.rekly.domain.model.StatusAssinatura
import java.time.LocalDate

/**
 * Assinatura ou conta recorrente.
 *
 * Valores em centavos ([Long]) para evitar erros de arredondamento de ponto flutuante.
 * [valorCentavos], [dataInicio] e [diaCobranca] ficam nulos nas assinaturas criadas
 * pelo onboarding até o usuário completá-las (ver [isCompleta]).
 */
@Entity(
    tableName = "assinaturas",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("usuarioId"), Index("categoriaId")]
)
data class Assinatura(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val categoriaId: Long,
    val nomeServico: String,
    /** Valor cobrado a cada período (não a mensalidade), em centavos. */
    val valorCentavos: Long? = null,
    /** Contas como água e luz: o pagamento gerado fica pendente até confirmar o valor real. */
    val valorVariavel: Boolean = false,
    val periodo: Periodo = Periodo.MENSAL,
    val dataInicio: LocalDate? = null,
    /** Dia do mês (1 a 31); em mês mais curto, cai no último dia. */
    val diaCobranca: Int? = null,
    val status: StatusAssinatura = StatusAssinatura.ATIVA,
    /** Pessoas que dividem o gasto, para o custo real (mínimo 1). */
    val participantes: Int = 1,
    /**
     * Data em que a assinatura passou a ser ATIVA pela última vez. Pagamentos
     * presumidos só são gerados a partir do mês desta data, então cadastrar uma
     * assinatura antiga ou converter um teste não cria pagamentos retroativos.
     */
    val ativaDesde: LocalDate? = null,
    val criadoEm: LocalDate
) {
    /** Tem os dados mínimos para entrar nos cálculos e gerar pagamentos. */
    fun isCompleta(): Boolean = valorCentavos != null && dataInicio != null && diaCobranca != null
}
