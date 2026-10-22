package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Assinatura
import com.example.rekly.domain.model.Periodo
import com.example.rekly.domain.model.StatusAssinatura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalculoGastosTest {

    private val hoje = LocalDate.of(2026, 10, 21)

    private fun assinatura(
        valorCentavos: Long? = 3990,
        periodo: Periodo = Periodo.MENSAL,
        status: StatusAssinatura = StatusAssinatura.ATIVA,
        participantes: Int = 1,
        dataInicio: LocalDate? = LocalDate.of(2026, 1, 10),
        diaCobranca: Int? = 10
    ) = Assinatura(
        usuarioId = 1,
        categoriaId = 1,
        nomeServico = "Netflix",
        valorCentavos = valorCentavos,
        periodo = periodo,
        dataInicio = dataInicio,
        diaCobranca = diaCobranca,
        status = status,
        participantes = participantes,
        criadoEm = hoje
    )

    // Mensalidade

    @Test
    fun mensalidade_mensal_eh_o_proprio_valor() {
        assertEquals(3990L, CalculoGastos.mensalidadeCentavos(3990, Periodo.MENSAL))
    }

    @Test
    fun mensalidade_divide_pelos_meses_do_periodo() {
        assertEquals(1330L, CalculoGastos.mensalidadeCentavos(3990, Periodo.TRIMESTRAL))
        assertEquals(1000L, CalculoGastos.mensalidadeCentavos(6000, Periodo.SEMESTRAL))
        assertEquals(1000L, CalculoGastos.mensalidadeCentavos(12000, Periodo.ANUAL))
    }

    @Test
    fun mensalidade_arredonda_meio_centavo_para_cima() {
        assertEquals(833L, CalculoGastos.mensalidadeCentavos(9999, Periodo.ANUAL)) // 833,25
        assertEquals(1L, CalculoGastos.mensalidadeCentavos(3, Periodo.SEMESTRAL)) // 0,5
        assertEquals(0L, CalculoGastos.mensalidadeCentavos(5, Periodo.ANUAL)) // 0,4166
        assertEquals(1L, CalculoGastos.mensalidadeCentavos(7, Periodo.ANUAL)) // 0,5833
    }

    // Custo real

    @Test
    fun custo_real_divide_pelos_participantes() {
        assertEquals(1995L, CalculoGastos.custoRealCentavos(3990, Periodo.MENSAL, 2))
        assertEquals(1330L, CalculoGastos.custoRealCentavos(3990, Periodo.MENSAL, 3))
    }

    @Test
    fun custo_real_combina_periodo_e_participantes() {
        // 120,00 por ano dividido entre 4 = 2,50 por mês
        assertEquals(250L, CalculoGastos.custoRealCentavos(12000, Periodo.ANUAL, 4))
    }

    @Test
    fun custo_real_considera_no_minimo_um_participante() {
        assertEquals(3990L, CalculoGastos.custoRealCentavos(3990, Periodo.MENSAL, 0))
        assertEquals(3990L, CalculoGastos.custoRealCentavos(3990, Periodo.MENSAL, -2))
    }

    // Vencimento

    @Test
    fun dia_31_em_mes_curto_cai_no_ultimo_dia() {
        assertEquals(LocalDate.of(2026, 2, 28), CalculoGastos.vencimentoNaCompetencia(YearMonth.of(2026, 2), 31))
        assertEquals(LocalDate.of(2028, 2, 29), CalculoGastos.vencimentoNaCompetencia(YearMonth.of(2028, 2), 31))
        assertEquals(LocalDate.of(2026, 4, 30), CalculoGastos.vencimentoNaCompetencia(YearMonth.of(2026, 4), 31))
        assertEquals(LocalDate.of(2026, 3, 31), CalculoGastos.vencimentoNaCompetencia(YearMonth.of(2026, 3), 31))
    }

    @Test
    fun proximo_vencimento_mensal_ja_passou_no_mes_vai_para_o_seguinte() {
        val proximo = CalculoGastos.proximoVencimento(LocalDate.of(2026, 1, 10), Periodo.MENSAL, 10, hoje)
        assertEquals(LocalDate.of(2026, 11, 10), proximo)
    }

    @Test
    fun proximo_vencimento_inclui_hoje() {
        val proximo = CalculoGastos.proximoVencimento(LocalDate.of(2026, 1, 21), Periodo.MENSAL, 21, hoje)
        assertEquals(hoje, proximo)
    }

    @Test
    fun proximo_vencimento_anual_segue_o_mes_de_inicio() {
        val proximo = CalculoGastos.proximoVencimento(LocalDate.of(2025, 3, 15), Periodo.ANUAL, 15, hoje)
        assertEquals(LocalDate.of(2027, 3, 15), proximo)
    }

    @Test
    fun proximo_vencimento_trimestral_com_dia_31() {
        // Ciclo: 31/01, 30/04, 31/07, 31/10
        val proximo = CalculoGastos.proximoVencimento(LocalDate.of(2026, 1, 31), Periodo.TRIMESTRAL, 31, hoje)
        assertEquals(LocalDate.of(2026, 10, 31), proximo)
    }

    @Test
    fun vencimento_anterior_a_data_de_inicio_eh_ignorado() {
        // Começou dia 20 com cobrança dia 5: o primeiro vencimento é no mês seguinte
        val primeiro = CalculoGastos.vencimentos(LocalDate.of(2026, 10, 20), Periodo.MENSAL, 5).first()
        assertEquals(YearMonth.of(2026, 11) to LocalDate.of(2026, 11, 5), primeiro)
    }

    // Versões com a assinatura

    @Test
    fun assinatura_incompleta_nao_tem_mensalidade_nem_vencimento() {
        val incompleta = assinatura(valorCentavos = null, dataInicio = null, diaCobranca = null)
        assertNull(CalculoGastos.mensalidadeCentavos(incompleta))
        assertNull(CalculoGastos.custoRealCentavos(incompleta))
        assertNull(CalculoGastos.proximoVencimento(incompleta, hoje))
    }

    @Test
    fun assinatura_cancelada_nao_tem_proximo_vencimento() {
        assertNull(CalculoGastos.proximoVencimento(assinatura(status = StatusAssinatura.CANCELADA), hoje))
    }

    @Test
    fun totais_consideram_so_ativas_e_completas() {
        val assinaturas = listOf(
            assinatura(valorCentavos = 3990, participantes = 2), // 39,90 / custo 19,95
            assinatura(valorCentavos = 12000, periodo = Periodo.ANUAL), // 10,00 / custo 10,00
            assinatura(valorCentavos = 2190, status = StatusAssinatura.TESTE),
            assinatura(valorCentavos = 5000, status = StatusAssinatura.CANCELADA),
            assinatura(valorCentavos = null)
        )
        assertEquals(4990L, CalculoGastos.totalMensalCentavos(assinaturas))
        assertEquals(2995L, CalculoGastos.totalCustoRealCentavos(assinaturas))
    }
}
