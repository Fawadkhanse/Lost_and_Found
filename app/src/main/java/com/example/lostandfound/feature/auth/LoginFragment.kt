package com.example.lostandfound.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * LoginFragment - Example implementation
 * Demonstrates how to use ViewModel with single API call pattern
 */
class LoginFragment : Fragment() {

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
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val userType = binding.spinnerUserType.selectedItem.toString().lowercase()

            if (validateInput(email, password)) {
                authViewModel.login(email, password, userType)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            // Navigate to forgot password
        }

//        binding.tvRegister.setOnClickListener {
//            // Navigate to register
//        }
    }

    private fun observeViewModel() {
        // Observe login state
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        showLoading(false)
                        val loginResponse = resource.data

                        // Save token
                        loginResponse.data?.token?.let { token ->
                            // Save to TokenManager or SharedPreferences
                        }

                        Toast.makeText(
                            requireContext(),
                            loginResponse.responseMessage,
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to home
                        navigateToHome()
                    }
                    is Resource.Failure -> {
                        showLoading(false)
                        Toast.makeText(
                            requireContext(),
                            resource.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Resource.None -> {
                        // Initial state
                    }
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return false
        }

        return true
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }

    private fun navigateToHome() {
        // Navigation logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}