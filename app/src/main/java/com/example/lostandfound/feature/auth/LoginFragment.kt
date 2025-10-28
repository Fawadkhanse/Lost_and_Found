package com.example.lostandfound.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.data.Resource
import com.example.lostandfound.data.TokenManager
import com.example.lostandfound.databinding.FragmentLoginBinding
import com.example.lostandfound.feature.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * LoginFragment - Example implementation
 * Demonstrates how to use ViewModel with single API call pattern
 */
class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Inject ViewModel using Koin
    private val authViewModel: AuthViewModel by viewModel()

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
        binding.btnLogin.setOnClickListener {
            val email = binding.etUserId.text.toString()
            val password = binding.etPassword.text.toString()
            val userType = binding.spinnerUserType.selectedItem.toString().lowercase()

            if (validateInput(email, password)) {
                authViewModel.login(email, password, userType)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            // Navigate to forgot password
            // findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

//        binding.tvRegister.setOnClickListener {
//            // Navigate to register
//            // findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
//        }
    }

    private fun observeViewModel() {
        // Observe login state
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        val loginResponse = resource.data

                        // Save token to TokenManager
                        loginResponse.data?.token?.let { token ->
                            TokenManager.setToken(token)
                            // Also save to SharedPreferences if needed for persistence
                        }

                        Toast.makeText(
                            requireContext(),
                            loginResponse.responseMessage,
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to home
                        navigateToHome()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        val errorMessage = resource.exception.message ?: "Login failed"
                        Toast.makeText(
                            requireContext(),
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Resource.None -> {
                        // Initial state - do nothing
                    }
                }
            }
        }

        // Observe logged in state
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    // Additional actions when user is logged in
                }
            }
        }

        // Observe current user
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                user?.let {
                    // Handle current user data if needed
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etUserId.error = "Email is required"
            return false
        }
//
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            binding.etEmail.error = "Please enter a valid email"
//            return false
//        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }


    private fun navigateToHome() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}