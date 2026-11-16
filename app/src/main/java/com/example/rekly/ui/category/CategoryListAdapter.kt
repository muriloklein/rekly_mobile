package com.example.rekly.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rekly.data.entity.Category
import com.example.rekly.databinding.ItemCategoryBinding

/** Default categories show the "Padrão" label; only the user's have rename and delete. */
class CategoryListAdapter(
    private val onRename: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.nameText.text = category.name
            binding.defaultLabel.isVisible = category.isDefault
            binding.renameButton.isVisible = !category.isDefault
            binding.deleteButton.isVisible = !category.isDefault
            binding.renameButton.setOnClickListener { onRename(category) }
            binding.deleteButton.setOnClickListener { onDelete(category) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(old: Category, new: Category): Boolean = old.id == new.id

        override fun areContentsTheSame(old: Category, new: Category): Boolean = old == new
    }
}
