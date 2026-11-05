package com.example.rekly.domain.rules

import com.example.rekly.domain.model.ErroRegra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadoresTest {

    @Test
    fun nome_vazio_e_obrigatorio() {
        assertEquals(ErroRegra.NOME_OBRIGATORIO, Validadores.validarNome("   "))
    }

    @Test
    fun nome_com_2_letras_e_curto() {
        assertEquals(ErroRegra.NOME_CURTO, Validadores.validarNome("Gu"))
    }

    @Test
    fun nome_com_espacos_nas_pontas_conta_so_as_letras() {
        assertEquals(ErroRegra.NOME_CURTO, Validadores.validarNome("  Gu  "))
    }

    @Test
    fun nome_com_3_letras_e_valido() {
        assertNull(Validadores.validarNome("Gui"))
    }

    @Test
    fun email_valido_passa() {
        assertNull(Validadores.validarEmail("guilherme@rekly.com"))
    }

    @Test
    fun email_com_espacos_nas_pontas_passa() {
        assertNull(Validadores.validarEmail("  guilherme@rekly.com "))
    }

    @Test
    fun email_sem_arroba_e_invalido() {
        assertEquals(ErroRegra.EMAIL_INVALIDO, Validadores.validarEmail("guilherme.rekly.com"))
    }

    @Test
    fun email_sem_dominio_e_invalido() {
        assertEquals(ErroRegra.EMAIL_INVALIDO, Validadores.validarEmail("guilherme@"))
    }

    @Test
    fun email_com_espaco_no_meio_e_invalido() {
        assertEquals(ErroRegra.EMAIL_INVALIDO, Validadores.validarEmail("gui lherme@rekly.com"))
    }

    @Test
    fun senha_vazia_no_login_e_obrigatoria() {
        assertEquals(ErroRegra.SENHA_OBRIGATORIA, Validadores.validarSenhaPreenchida(""))
    }

    @Test
    fun senha_preenchida_no_login_passa() {
        assertNull(Validadores.validarSenhaPreenchida("1"))
    }

    @Test
    fun senha_nova_com_5_caracteres_e_curta() {
        assertEquals(ErroRegra.SENHA_CURTA, Validadores.validarSenhaNova("12345"))
    }

    @Test
    fun senha_nova_com_6_caracteres_e_valida() {
        assertNull(Validadores.validarSenhaNova("123456"))
    }

    @Test
    fun confirmacao_diferente_da_senha_e_invalida() {
        assertEquals(ErroRegra.SENHAS_DIFERENTES, Validadores.validarConfirmacao("123456", "123457"))
    }

    @Test
    fun confirmacao_igual_a_senha_passa() {
        assertNull(Validadores.validarConfirmacao("123456", "123456"))
    }
}
