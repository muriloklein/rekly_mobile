package com.example.rekly.domain.rules

import com.example.rekly.domain.model.RuleError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun blank_name_is_required() {
        assertEquals(RuleError.NAME_REQUIRED, Validators.validateName("   "))
    }

    @Test
    fun name_with_2_letters_is_too_short() {
        assertEquals(RuleError.NAME_TOO_SHORT, Validators.validateName("Gu"))
    }

    @Test
    fun name_with_surrounding_spaces_counts_only_the_letters() {
        assertEquals(RuleError.NAME_TOO_SHORT, Validators.validateName("  Gu  "))
    }

    @Test
    fun name_with_3_letters_is_valid() {
        assertNull(Validators.validateName("Gui"))
    }

    @Test
    fun valid_email_passes() {
        assertNull(Validators.validateEmail("guilherme@rekly.com"))
    }

    @Test
    fun email_with_surrounding_spaces_passes() {
        assertNull(Validators.validateEmail("  guilherme@rekly.com "))
    }

    @Test
    fun email_without_at_sign_is_invalid() {
        assertEquals(RuleError.INVALID_EMAIL, Validators.validateEmail("guilherme.rekly.com"))
    }

    @Test
    fun email_without_domain_is_invalid() {
        assertEquals(RuleError.INVALID_EMAIL, Validators.validateEmail("guilherme@"))
    }

    @Test
    fun email_with_space_in_the_middle_is_invalid() {
        assertEquals(RuleError.INVALID_EMAIL, Validators.validateEmail("gui lherme@rekly.com"))
    }

    @Test
    fun empty_login_password_is_required() {
        assertEquals(RuleError.PASSWORD_REQUIRED, Validators.validatePasswordFilled(""))
    }

    @Test
    fun filled_login_password_passes() {
        assertNull(Validators.validatePasswordFilled("1"))
    }

    @Test
    fun new_password_with_5_characters_is_too_short() {
        assertEquals(RuleError.PASSWORD_TOO_SHORT, Validators.validateNewPassword("12345"))
    }

    @Test
    fun new_password_with_6_characters_is_valid() {
        assertNull(Validators.validateNewPassword("123456"))
    }

    @Test
    fun confirmation_different_from_password_is_invalid() {
        assertEquals(RuleError.PASSWORDS_DO_NOT_MATCH, Validators.validateConfirmation("123456", "123457"))
    }

    @Test
    fun confirmation_equal_to_password_passes() {
        assertNull(Validators.validateConfirmation("123456", "123456"))
    }
}
