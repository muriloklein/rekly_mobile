package com.example.rekly.ui.category

import android.app.Dialog
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.rekly.R
import com.example.rekly.databinding.DialogCategoryNameBinding
import com.example.rekly.util.SessionManager
import com.example.rekly.util.messageRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Dialog to create or rename a category.
 *
 * It is a DialogFragment (not a plain AlertDialog) so the FragmentManager recreates it after
 * a rotation. The Save button is wired in onStart because the default button always closes the
 * dialog, and here it has to stay open to show the error in the field.
 */
class CategoryNameDialog : DialogFragment() {

    private var _binding: DialogCategoryNameBinding? = null
    private val binding get() = _binding!!
    private var clearsErrorOnTyping = false

    // Same instance as the activity's; the factory is only used if the process was recreated
    private val viewModel: CategoriesViewModel by activityViewModels {
        CategoriesActivity.viewModelFactory(requireContext(), SessionManager(requireContext()).loggedUserId() ?: 0)
    }

    private val categoryId: Long?
        get() = requireArguments().getLong(ARG_CATEGORY_ID, NO_ID).takeIf { it != NO_ID }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCategoryNameBinding.inflate(layoutInflater)
        if (savedInstanceState == null) {
            binding.nameInput.setText(requireArguments().getString(ARG_CURRENT_NAME))
            binding.nameInput.setSelection(binding.nameInput.length())
            viewModel.onNameDialogOpened()
        }
        binding.nameInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) save()
            actionId == EditorInfo.IME_ACTION_DONE
        }
        observeViewModel()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (categoryId == null) R.string.title_new_category else R.string.title_rename_category)
            .setView(binding.root)
            .setNegativeButton(R.string.action_back, null)
            .setPositiveButton(R.string.action_save, null)
            .create()
    }

    override fun onStart() {
        super.onStart()
        (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener { save() }
        // Added only now: after a rotation the dialog puts the typed text back before onStart,
        // and that would count as typing and wipe the error. The flag avoids a second listener
        // when the app comes back from the background.
        if (!clearsErrorOnTyping) {
            binding.nameInput.doAfterTextChanged { viewModel.clearNameError() }
            clearsErrorOnTyping = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewModel.nameError.observe(this) { error ->
            binding.nameLayout.error = error?.let { getString(it.messageRes()) }
        }
        viewModel.nameSaved.observe(this) { saved ->
            if (!saved) return@observe
            viewModel.onNameSavedHandled()
            dismiss()
        }
    }

    private fun save() {
        viewModel.saveName(categoryId, binding.nameInput.text?.toString().orEmpty())
    }

    companion object {
        const val TAG = "category_name_dialog"
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_CURRENT_NAME = "current_name"
        private const val NO_ID = -1L

        /** Without arguments it creates a new category; with them it renames that one. */
        fun newInstance(categoryId: Long? = null, currentName: String = ""): CategoryNameDialog =
            CategoryNameDialog().apply {
                arguments = bundleOf(ARG_CATEGORY_ID to (categoryId ?: NO_ID), ARG_CURRENT_NAME to currentName)
            }
    }
}
