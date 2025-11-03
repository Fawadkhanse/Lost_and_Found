package com.example.lostandfound.feature.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentLoginBinding
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * LoginFragment - Enhanced with Show/Hide Password functionality
 * Features:
 * - Email/password login
 * - Show/hide password toggle
 * - Input validation
 * - Error handling
 * - Navigation to dashboard based on user type
 */
class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    // Track password visibility state
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Login button
        binding.btnLogin.setOnClickListener {
            // For testing - pre-fill credentials
            // binding.etUserId.setText("b@b.com")
            // binding.etPassword.setText("12345678p")

            val email = binding.etUserId.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateInputs(email, password)) {
                authViewModel.login(email, password)
            }
        }

        // Show/Hide Password Toggle
        binding.ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment2)
        }

        // Register
//        binding.tvRegister.setOnClickListener {
//            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
//        }
    }

    /**
     * Toggle password visibility
     * Changes between showing plain text and password dots
     */
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye_off)
            isPasswordVisible = false
        } else {
            // Show password
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye)
            isPasswordVisible = true
        }

        // Move cursor to end of text
        binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Logging in...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        val response = resource.data
                        AuthData.setAuthResponse(resource.data)
//                        Toast.makeText(
//                            requireContext(),
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()

                        // Navigate based on user type
                        when (response.user.userType) {
                            "admin" -> findNavController().navigate(R.id.action_loginFragment_to_adminHomeFragment)
                            "resident" -> findNavController().navigate(R.id.action_loginFragment_to_residentHomeFragment)
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError(resource.exception.message)
                    }
                    Resource.None -> {
                        // Initial state
                    }
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etUserId.error = "Email is required"
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}