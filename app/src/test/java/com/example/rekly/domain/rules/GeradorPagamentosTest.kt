package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Assinatura
import com.example.rekly.domain.model.Periodo
import com.example.rekly.domain.model.StatusAssinatura
import com.example.rekly.domain.model.StatusPagamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class GeradorPagamentosTest {

    private val hoje = LocalDate.of(2026, 10, 21)

    private fun assinatura(
        valorCentavos: Long? = 3990,
        valorVariavel: Boolean = false,
        periodo: Periodo = Periodo.MENSAL,
        dataInicio: LocalDate? = LocalDate.of(2026, 1, 10),
        diaCobranca: Int? = 10,
        status: StatusAssinatura = StatusAssinatura.ATIVA,
        ativaDesde: LocalDate? = LocalDate.of(2026, 10, 1)
    ) = Assinatura(
        id = 7,
        usuarioId = 1,
        categoriaId = 1,
        nomeServico = "Netflix",
        valorCentavos = valorCentavos,
        valorVariavel = valorVariavel,
        periodo = periodo,
        dataInicio = dataInicio,
        diaCobranca = diaCobranca,
        status = status,
        ativaDesde = ativaDesde,
        criadoEm = LocalDate.of(2026, 10, 1)
    )

    private fun gerar(
        assinatura: Assinatura,
        existentes: Set<YearMonth> = emptySet(),
        ultimoValorPago: Long? = null,
        hoje: LocalDate = this.hoje
    ) = GeradorPagamentos.gerar(assinatura, existentes, ultimoValorPago, hoje)

    // Valor fixo e variável

    @Test
    fun valor_fixo_gera_pago_no_vencimento() {
        val pagamentos = gerar(assinatura())

        assertEquals(1, pagamentos.size)
        with(pagamentos.single()) {
            assertEquals(7L, assinaturaId)
            assertEquals(YearMonth.of(2026, 10), competencia)
            assertEquals(3990L, valorCentavos)
            assertEquals(LocalDate.of(2026, 10, 10), dataVencimento)
            assertEquals(LocalDate.of(2026, 10, 10), dataPagamento)
            assertEquals(StatusPagamento.PAGO, status)
        }
    }

    @Test
    fun valor_variavel_gera_pendente_com_ultimo_valor_pago() {
        val pagamento = gerar(assinatura(valorCentavos = 15000, valorVariavel = true), ultimoValorPago = 18734).single()

        assertEquals(StatusPagamento.PENDENTE, pagamento.status)
        assertEquals(18734L, pagamento.valorCentavos)
        assertNull(pagamento.dataPagamento)
    }

    @Test
    fun valor_variavel_sem_historico_usa_valor_da_assinatura() {
        val pagamento = gerar(assinatura(valorCentavos = 15000, valorVariavel = true)).single()
        assertEquals(15000L, pagamento.valorCentavos)
    }

    // Quando gerar

    @Test
    fun nao_gera_antes_do_vencimento() {
        assertTrue(gerar(assinatura(), hoje = LocalDate.of(2026, 10, 9)).isEmpty())
    }

    @Test
    fun gera_no_proprio_dia_do_vencimento() {
        assertEquals(1, gerar(assinatura(), hoje = LocalDate.of(2026, 10, 10)).size)
    }

    @Test
    fun nao_gera_retroativo_antes_do_mes_de_ativacao() {
        val antiga = assinatura(dataInicio = LocalDate.of(2022, 5, 10), ativaDesde = LocalDate.of(2026, 10, 15))
        val competencias = gerar(antiga, hoje = LocalDate.of(2026, 12, 15)).map { it.competencia }

        assertEquals(
            listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 11), YearMonth.of(2026, 12)),
            competencias
        )
    }

    @Test
    fun nao_duplica_competencias_existentes() {
        val competencias = gerar(
            assinatura(),
            existentes = setOf(YearMonth.of(2026, 10), YearMonth.of(2026, 11)),
            hoje = LocalDate.of(2026, 12, 15)
        ).map { it.competencia }

        assertEquals(listOf(YearMonth.of(2026, 12)), competencias)
    }

    @Test
    fun anual_gera_um_pagamento_por_ano() {
        val anual = assinatura(
            valorCentavos = 12000,
            periodo = Periodo.ANUAL,
            dataInicio = LocalDate.of(2025, 3, 15),
            diaCobranca = 15,
            ativaDesde = LocalDate.of(2025, 3, 15)
        )
        val competencias = gerar(anual).map { it.competencia }

        assertEquals(listOf(YearMonth.of(2025, 3), YearMonth.of(2026, 3)), competencias)
    }

    // Quando não gerar

    @Test
    fun teste_e_cancelada_nao_geram() {
        assertTrue(gerar(assinatura(status = StatusAssinatura.TESTE)).isEmpty())
        assertTrue(gerar(assinatura(status = StatusAssinatura.CANCELADA)).isEmpty())
    }

    @Test
    fun incompleta_nao_gera() {
        assertTrue(gerar(assinatura(valorCentavos = null)).isEmpty())
        assertTrue(gerar(assinatura(dataInicio = null)).isEmpty())
        assertTrue(gerar(assinatura(diaCobranca = null)).isEmpty())
    }

    // Ativação

    @Test
    fun criar_ativa_marca_ativa_desde_hoje() {
        val nova = GeradorPagamentos.ajustarAtivacao(null, assinatura(ativaDesde = null), hoje)
        assertEquals(hoje, nova.ativaDesde)
    }

    @Test
    fun criar_em_teste_nao_marca_ativacao() {
        val nova = GeradorPagamentos.ajustarAtivacao(
            null,
            assinatura(status = StatusAssinatura.TESTE, ativaDesde = null),
            hoje
        )
        assertNull(nova.ativaDesde)
    }

    @Test
    fun converter_teste_em_ativa_marca_hoje() {
        val anterior = assinatura(status = StatusAssinatura.TESTE, ativaDesde = null)
        val nova = GeradorPagamentos.ajustarAtivacao(anterior, anterior.copy(status = StatusAssinatura.ATIVA), hoje)
        assertEquals(hoje, nova.ativaDesde)
    }

    @Test
    fun reativar_cancelada_marca_hoje() {
        val anterior = assinatura(status = StatusAssinatura.CANCELADA, ativaDesde = LocalDate.of(2025, 1, 1))
        val nova = GeradorPagamentos.ajustarAtivacao(anterior, anterior.copy(status = StatusAssinatura.ATIVA), hoje)
        assertEquals(hoje, nova.ativaDesde)
    }

    @Test
    fun editar_ativa_mantem_data_de_ativacao() {
        val anterior = assinatura(ativaDesde = LocalDate.of(2026, 3, 1))
        val nova = GeradorPagamentos.ajustarAtivacao(anterior, anterior.copy(valorCentavos = 4590), hoje)
        assertEquals(LocalDate.of(2026, 3, 1), nova.ativaDesde)
    }

    @Test
    fun cancelar_mantem_data_de_ativacao() {
        val anterior = assinatura(ativaDesde = LocalDate.of(2026, 3, 1))
        val nova = GeradorPagamentos.ajustarAtivacao(
            anterior,
            anterior.copy(status = StatusAssinatura.CANCELADA),
            hoje
        )
        assertEquals(LocalDate.of(2026, 3, 1), nova.ativaDesde)
    }
}
