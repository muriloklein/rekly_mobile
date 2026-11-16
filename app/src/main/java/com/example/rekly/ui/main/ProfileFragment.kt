package com.example.rekly.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rekly.R
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.repository.UserRepository
import com.example.rekly.databinding.FragmentProfileBinding
import com.example.rekly.ui.auth.LoginActivity
import com.example.rekly.ui.category.CategoriesActivity
import com.example.rekly.util.SessionManager

/** User data, access to categories and the Log out button. */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    // The view can be destroyed before the fragment itself; releasing the binding in
    // onDestroyView avoids holding a view that is no longer on screen.
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var userId: Long = 0

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(UserRepository(AppDatabase.getInstance(requireContext()).userDao()), userId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        binding.categoriesButton.setOnClickListener {
            startActivity(CategoriesActivity.newIntent(requireContext()))
        }
        binding.logoutButton.setOnClickListener { logout() }

        userId = SessionManager(requireContext()).loggedUserId() ?: return
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.nameText.text = user.name
            binding.emailText.text = user.email
        }
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
