package com.example.rekly.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.rekly.R
import com.example.rekly.databinding.FragmentProfileBinding
import com.example.rekly.ui.auth.LoginActivity
import com.example.rekly.util.SessionManager

/** User data, access to categories and the Log out button (tasks 3 and 7). */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    // The view can be destroyed before the fragment itself; releasing the binding in
    // onDestroyView avoids holding a view that is no longer on screen.
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        binding.logoutButton.setOnClickListener { logout() }
    }

    private fun logout() {
        SessionManager(requireContext()).clear()
        startActivity(LoginActivity.newTask(requireContext()))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
