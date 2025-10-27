package com.example.lostandfound.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.lostandfound.R
import com.example.lostandfound.data.model.Resource
import com.example.lostandfound.databinding.FragmentLoginBinding
import com.example.lostandfound.presentation.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // ViewModel initialization
    private val viewModel: AuthViewModel by viewModels()

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
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        // Setup Spinner
        val userTypes = arrayOf("Student", "Staff", "Admin")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            userTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUserType.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.tvForgotPassword.setOnClickListener {
            // Navigate to forgot password
            // findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    private fun observeViewModel() {
        // Observe login state
        viewModel.loginState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Login successful!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to home
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Login failed: ${resource.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    // Handle None state
                }
            }
        }
    }

    private fun handleLogin() {
        val userId = binding.etUserId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val userType = binding.spinnerUserType.selectedItem.toString().lowercase()

        // Validate inputs
        if (userId.isEmpty()) {
            binding.etUserId.error = "User ID is required"
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return
        }

        // Call ViewModel to perform login
        viewModel.login(userId, password, userType)
    }

    private fun showLoading(show: Boolean) {
        binding.btnLogin.isEnabled = !show
        binding.btnLogin.text = if (show) "Logging in..." else "Log In"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}