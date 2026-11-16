package com.example.rekly.ui.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.rekly.R
import com.example.rekly.data.entity.Category
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.repository.CategoryRepository
import com.example.rekly.databinding.ActivityCategoriesBinding
import com.example.rekly.ui.auth.LoginActivity
import com.example.rekly.util.SessionManager
import com.example.rekly.util.applyFormScreenInsets
import com.example.rekly.util.messageRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/** Category CRUD: lists default and personal categories, creates, renames and deletes. */
class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private var userId: Long = 0

    private val viewModel: CategoriesViewModel by viewModels { viewModelFactory(this, userId) }

    private val adapter = CategoryListAdapter(onRename = ::openRename, onDelete = ::confirmDelete)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loggedUserId = SessionManager(this).loggedUserId()
        if (loggedUserId == null) {
            openLogin()
            return
        }
        userId = loggedUserId

        enableEdgeToEdge()
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyFormScreenInsets(binding.root, binding.toolbar.root)

        binding.categoryList.adapter = adapter
        binding.fabNewCategory.setOnClickListener { openCreate() }

        viewModel.categories.observe(this) { adapter.submitList(it) }
        viewModel.error.observe(this) { error ->
            error ?: return@observe
            Snackbar.make(binding.root, error.messageRes(), Snackbar.LENGTH_LONG)
                .setAnchorView(binding.fabNewCategory)
                .show()
            viewModel.onErrorShown()
        }
    }

    override fun onStart() {
        super.onStart()
        if (SessionManager(this).loggedUserId() == null) openLogin()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun openCreate() = showNameDialog(CategoryNameDialog.newInstance())

    private fun openRename(category: Category) =
        showNameDialog(CategoryNameDialog.newInstance(category.id, category.name))

    // Avoids two dialogs on top of each other after a quick double tap
    private fun showNameDialog(dialog: CategoryNameDialog) {
        if (supportFragmentManager.findFragmentByTag(CategoryNameDialog.TAG) != null) return
        dialog.show(supportFragmentManager, CategoryNameDialog.TAG)
    }

    private fun confirmDelete(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_category_title)
            .setMessage(getString(R.string.dialog_delete_category_message, category.name))
            .setNegativeButton(R.string.action_back, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.delete(category.id) }
            .show()
    }

    private fun openLogin() {
        startActivity(LoginActivity.newTask(this))
        finish()
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, CategoriesActivity::class.java)

        /** Also used by [CategoryNameDialog], which shares this screen's ViewModel. */
        fun viewModelFactory(context: Context, userId: Long): ViewModelProvider.Factory {
            val database = AppDatabase.getInstance(context)
            return CategoriesViewModel.factory(
                CategoryRepository(database.categoryDao(), database.subscriptionDao()),
                userId
            )
        }
    }
}
