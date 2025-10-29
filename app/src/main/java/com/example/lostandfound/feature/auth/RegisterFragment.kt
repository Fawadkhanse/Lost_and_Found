package com.example.lostandfound.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentRegisterBinding
import com.example.lostandfound.domain.auth.RegisterRequest
import com.example.lostandfound.feature.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : BaseFragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCreate.setOnClickListener {
            val userId = binding.etUserId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val faculty = binding.etFaculty.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            if (validateInputs(userId, password, phone, email, faculty, address)) {
                val request = RegisterRequest(
                    username = userId,
                    email = email,
                    password = password,
                    password2 = password,
                    firstName = "home",
                    lastName = "Doe",
                    userType = "resident",
                    phoneNumber = phone,
                    towerNumber = address,
                    roomNumber = "101",
                    profileImage = "file.png"

                )
                authViewModel.register(
                    request
                )
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.registerState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Creating account...")
                    is Resource.Success -> {
                        hideLoading()
                        Toast.makeText(
                            requireContext(),
                            "Registration successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToLogin()
                    }
                    is Resource.Error -> {
                        hideLoading()
                    //    showError()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun validateInputs(
        userId: String,
        password: String,
        phone: String,
        email: String,
        faculty: String,
        address: String
    ): Boolean {
        return when {
            userId.isEmpty() -> {
                binding.etUserId.error = "User ID is required"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password is required"
                false
            }
            phone.isEmpty() -> {
                binding.etPhone.error = "Phone number is required"
                false
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email is required"
                false
            }
            faculty.isEmpty() -> {
                binding.etFaculty.error = "Faculty is required"
                false
            }
            address.isEmpty() -> {
                binding.etAddress.error = "Address is required"
                false
            }
            else -> true
        }
    }

    private fun navigateToLogin() {
        // Example navigation logic — adjust if using Navigation Component
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
