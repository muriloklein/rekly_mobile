package com.example.rekly.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import com.example.rekly.R
import com.example.rekly.databinding.BottomSheetConfirmPaymentBinding
import com.example.rekly.domain.rules.Validators
import com.example.rekly.util.MoneyFormatter
import com.example.rekly.util.messageRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Panel that opens over the subscription details to confirm a pending payment with its real amount.
 *
 * The amount is validated here, so the error shows on the field itself, and only a valid
 * amount goes back through the Fragment Result API, together with the payment id.
 * The FragmentManager restores the panel and the typed text after a rotation.
 */
class ConfirmPaymentBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetConfirmPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetConfirmPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        binding.titleText.text = getString(R.string.payment_confirm_title, args.getString(ARG_MONTH))
        if (savedInstanceState == null) binding.amountInput.setText(args.getString(ARG_ESTIMATE))
        binding.amountInput.doAfterTextChanged { binding.amountLayout.error = null }
        binding.confirmButton.setOnClickListener { confirm(args.getLong(ARG_PAYMENT_ID)) }
        // Starts fully open so the keyboard does not hide the field in landscape
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun confirm(paymentId: Long) {
        val amountCents = MoneyFormatter.parseCents(binding.amountInput.text?.toString().orEmpty())
        val error = Validators.validateAmount(amountCents)
        if (error != null || amountCents == null) {
            binding.amountLayout.error = getString(error?.messageRes() ?: R.string.error_invalid_amount)
            return
        }
        setFragmentResult(REQUEST_KEY, bundleOf(RESULT_PAYMENT_ID to paymentId, RESULT_AMOUNT_CENTS to amountCents))
        dismiss()
    }

    companion object {
        const val TAG = "confirm_payment_bottom_sheet"
        const val REQUEST_KEY = "confirm_payment_request"
        private const val ARG_PAYMENT_ID = "payment_id"
        private const val ARG_MONTH = "month"
        private const val ARG_ESTIMATE = "estimate"
        private const val RESULT_PAYMENT_ID = "result_payment_id"
        private const val RESULT_AMOUNT_CENTS = "result_amount_cents"

        fun newInstance(paymentId: Long, month: String, estimateCents: Long) = ConfirmPaymentBottomSheet().apply {
            arguments = bundleOf(
                ARG_PAYMENT_ID to paymentId,
                ARG_MONTH to month,
                ARG_ESTIMATE to MoneyFormatter.toInputText(estimateCents)
            )
        }

        fun paymentId(result: Bundle): Long = result.getLong(RESULT_PAYMENT_ID)

        fun amountCents(result: Bundle): Long = result.getLong(RESULT_AMOUNT_CENTS)
    }
}
