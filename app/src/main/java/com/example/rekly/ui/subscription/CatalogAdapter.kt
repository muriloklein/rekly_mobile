package com.example.rekly.ui.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rekly.R
import com.example.rekly.databinding.ItemCatalogHeaderBinding
import com.example.rekly.databinding.ItemCatalogServiceBinding
import com.example.rekly.domain.catalog.CatalogService
import com.example.rekly.util.labelRes

/** A row of the catalog list: a category title or a service that can be chosen. */
sealed interface CatalogRow {
    data class Header(val categoryName: String) : CatalogRow
    data class Service(val service: CatalogService) : CatalogRow
}

/** The catalog is a fixed list, so a plain adapter is enough (no DiffUtil). */
class CatalogAdapter(
    private val rows: List<CatalogRow>,
    private val onServiceClick: (CatalogService) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = rows.size

    override fun getItemViewType(position: Int): Int = when (rows[position]) {
        is CatalogRow.Header -> TYPE_HEADER
        is CatalogRow.Service -> TYPE_SERVICE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemCatalogHeaderBinding.inflate(inflater, parent, false))
        } else {
            ServiceViewHolder(ItemCatalogServiceBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is CatalogRow.Header -> (holder as HeaderViewHolder).bind(row)
            is CatalogRow.Service -> (holder as ServiceViewHolder).bind(row.service)
        }
    }

    private class HeaderViewHolder(
        private val binding: ItemCatalogHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: CatalogRow.Header) {
            binding.categoryText.text = header.categoryName
        }
    }

    private inner class ServiceViewHolder(
        private val binding: ItemCatalogServiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(service: CatalogService) {
            val context = binding.root.context
            val period = context.getString(service.period.labelRes())
            binding.nameText.text = service.name
            binding.detailText.text = if (service.variableAmount) {
                context.getString(R.string.catalog_detail_variable, period)
            } else {
                period
            }
            binding.root.setOnClickListener { onServiceClick(service) }
        }
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_SERVICE = 1
    }
}
