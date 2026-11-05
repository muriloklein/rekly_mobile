package com.example.rekly.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.rekly.R
import com.example.rekly.databinding.FragmentSubscriptionsBinding

/** Subscription list with search and status filter (task 6). */
class SubscriptionsFragment : Fragment(R.layout.fragment_subscriptions) {

    // The view can be destroyed before the fragment itself; releasing the binding in
    // onDestroyView avoids holding a view that is no longer on screen.
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSubscriptionsBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
