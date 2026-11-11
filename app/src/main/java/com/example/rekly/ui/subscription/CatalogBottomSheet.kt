package com.example.rekly.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.rekly.data.local.DefaultCategories
import com.example.rekly.databinding.BottomSheetCatalogBinding
import com.example.rekly.domain.catalog.ServiceCatalog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Panel that opens over the subscription form to choose a service from the catalog.
 *
 * The choice goes back through the Fragment Result API, with the service id only.
 * The FragmentManager restores the panel after a rotation and the screen registers
 * its listener again, so the choice is not lost.
 */
class CatalogBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCatalogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.otherButton.setOnClickListener { choose(serviceId = null) }
        binding.servicesList.adapter = CatalogAdapter(buildRows()) { service -> choose(service.id) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun buildRows(): List<CatalogRow> =
        ServiceCatalog.groupedByCategory().flatMap { (categoryId, services) ->
            val header = CatalogRow.Header(DefaultCategories.all[categoryId].orEmpty())
            listOf(header) + services.map { CatalogRow.Service(it) }
        }

    /** A null id means "Other": the user fills in everything by hand. */
    private fun choose(serviceId: String?) {
        setFragmentResult(REQUEST_KEY, bundleOf(RESULT_SERVICE_ID to serviceId))
        dismiss()
    }

    companion object {
        const val TAG = "catalog_bottom_sheet"
        const val REQUEST_KEY = "catalog_request"
        private const val RESULT_SERVICE_ID = "service_id"

        fun chosenServiceId(result: Bundle): String? = result.getString(RESULT_SERVICE_ID)
    }
}
