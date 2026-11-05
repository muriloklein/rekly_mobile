package com.example.rekly.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.rekly.R
import com.example.rekly.databinding.FragmentSummaryBinding

/** Home tab: monthly totals, upcoming due dates, pending payments and suggestions (task 10). */
class SummaryFragment : Fragment(R.layout.fragment_summary) {

    // The view can be destroyed before the fragment itself; releasing the binding in
    // onDestroyView avoids holding a view that is no longer on screen.
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSummaryBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
