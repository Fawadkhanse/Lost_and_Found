package com.example.lostandfound.feature.auth

import android.os.Bundle
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
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * LoginFragment - Example implementation
 * Demonstrates how to use ViewModel with single API call pattern
 */
class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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

            if (validateInputs(email, password)) {
                authViewModel.login(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            // Navigate to forgot password screen
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment2)
        }

        binding.tvRegister.setOnClickListener {
           findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
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
                        Toast.makeText(
                            requireContext(),
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate based on user type
                        when (response.user.userType) {
                            "admin" ->  findNavController().navigate(R.id.action_loginFragment_to_adminHomeFragment)
                            "resident" ->findNavController().navigate(R.id.action_loginFragment_to_residentHomeFragment)
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


