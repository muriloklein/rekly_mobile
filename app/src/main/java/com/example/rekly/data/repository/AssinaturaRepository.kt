package com.example.rekly.data.repository

import androidx.lifecycle.LiveData
import com.example.rekly.data.entity.Assinatura
import com.example.rekly.data.entity.AssinaturaComCategoria
import com.example.rekly.data.entity.AssinaturaDetalhada
import com.example.rekly.data.local.AssinaturaDao
import com.example.rekly.data.local.PagamentoDao
import com.example.rekly.domain.model.ErroRegra
import com.example.rekly.domain.model.Resultado
import com.example.rekly.domain.model.StatusAssinatura
import com.example.rekly.domain.rules.GeradorPagamentos
import java.time.LocalDate

class AssinaturaRepository(
    private val assinaturaDao: AssinaturaDao,
    private val pagamentoDao: PagamentoDao
) {

    fun observarDoUsuario(usuarioId: Long): LiveData<List<AssinaturaComCategoria>> =
        assinaturaDao.observarComCategoria(usuarioId)

    fun observarDetalhada(id: Long): LiveData<AssinaturaDetalhada?> =
        assinaturaDao.observarDetalhada(id)

    suspend fun buscarPorId(id: Long): Assinatura? = assinaturaDao.buscarPorId(id)

    suspend fun criar(assinatura: Assinatura, hoje: LocalDate = LocalDate.now()): Long =
        assinaturaDao.inserir(GeradorPagamentos.ajustarAtivacao(null, assinatura, hoje))

    /** Criação em lote do onboarding (assinaturas ainda sem valor). */
    suspend fun criarEmLote(assinaturas: List<Assinatura>, hoje: LocalDate = LocalDate.now()): List<Long> =
        assinaturaDao.inserirTodas(assinaturas.map { GeradorPagamentos.ajustarAtivacao(null, it, hoje) })

    suspend fun atualizar(assinatura: Assinatura, hoje: LocalDate = LocalDate.now()): Resultado<Unit> {
        val anterior = assinaturaDao.buscarPorId(assinatura.id)
            ?: return Resultado.Falha(ErroRegra.NAO_ENCONTRADO)
        assinaturaDao.atualizar(GeradorPagamentos.ajustarAtivacao(anterior, assinatura, hoje))
        return Resultado.Sucesso(Unit)
    }

    /** Cancelar mantém o histórico e interrompe a geração de novos pagamentos. */
    suspend fun cancelar(assinatura: Assinatura): Resultado<Unit> =
        atualizar(assinatura.copy(status = StatusAssinatura.CANCELADA))

    /** Exclusão bloqueada se houver pagamentos; nesse caso a alternativa é cancelar. */
    suspend fun excluir(assinatura: Assinatura): Resultado<Unit> {
        if (pagamentoDao.contarPorAssinatura(assinatura.id) > 0) {
            return Resultado.Falha(ErroRegra.ASSINATURA_COM_PAGAMENTOS)
        }
        assinaturaDao.excluir(assinatura)
        return Resultado.Sucesso(Unit)
    }
}
