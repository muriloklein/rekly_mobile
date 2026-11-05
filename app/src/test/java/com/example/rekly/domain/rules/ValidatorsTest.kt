package com.example.rekly.domain.rules

import com.example.rekly.domain.model.RuleError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ValidatorsTest {

    private val today = LocalDate.of(2026, 11, 4)

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

    // Subscription form

    @Test
    fun blank_service_name_is_required() {
        assertEquals(RuleError.NAME_REQUIRED, Validators.validateServiceName("  "))
    }

    @Test
    fun service_name_with_one_letter_is_valid() {
        assertNull(Validators.validateServiceName("X"))
    }

    @Test
    fun missing_category_is_required() {
        assertEquals(RuleError.CATEGORY_REQUIRED, Validators.validateCategory(null))
    }

    @Test
    fun chosen_category_passes() {
        assertNull(Validators.validateCategory(1))
    }

    @Test
    fun missing_or_non_positive_amount_is_invalid() {
        assertEquals(RuleError.INVALID_AMOUNT, Validators.validateAmount(null))
        assertEquals(RuleError.INVALID_AMOUNT, Validators.validateAmount(0))
        assertEquals(RuleError.INVALID_AMOUNT, Validators.validateAmount(-100))
    }

    @Test
    fun amount_of_one_cent_is_valid() {
        assertNull(Validators.validateAmount(1))
    }

    @Test
    fun billing_day_outside_1_to_31_is_invalid() {
        assertEquals(RuleError.INVALID_BILLING_DAY, Validators.validateBillingDay(null))
        assertEquals(RuleError.INVALID_BILLING_DAY, Validators.validateBillingDay(0))
        assertEquals(RuleError.INVALID_BILLING_DAY, Validators.validateBillingDay(32))
    }

    @Test
    fun billing_days_1_and_31_are_valid() {
        assertNull(Validators.validateBillingDay(1))
        assertNull(Validators.validateBillingDay(31))
    }

    @Test
    fun zero_or_missing_participants_are_invalid() {
        assertEquals(RuleError.INVALID_PARTICIPANTS, Validators.validateParticipants(null))
        assertEquals(RuleError.INVALID_PARTICIPANTS, Validators.validateParticipants(0))
    }

    @Test
    fun one_participant_is_valid() {
        assertNull(Validators.validateParticipants(1))
    }

    @Test
    fun missing_start_date_is_required() {
        assertEquals(RuleError.START_DATE_REQUIRED, Validators.validateStartDate(null, today))
    }

    @Test
    fun future_start_date_is_invalid() {
        assertEquals(RuleError.START_DATE_IN_FUTURE, Validators.validateStartDate(today.plusDays(1), today))
    }

    @Test
    fun start_date_today_or_in_the_past_is_valid() {
        assertNull(Validators.validateStartDate(today, today))
        assertNull(Validators.validateStartDate(LocalDate.of(2022, 5, 10), today))
    }
}
