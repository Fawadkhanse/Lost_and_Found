// app/src/main/java/com/example/lostandfound/feature/auth/ForgotPasswordFragment.kt
package com.example.lostandfound.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentForgotPasswordBinding
import com.example.lostandfound.feature.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * ForgotPasswordFragment - Handle password reset requests
 * Flow:
 * 1. User enters email
 * 2. System verifies email exists
 * 3. Shows success message (in production, would send reset email)
 * 4. Navigate back to login
 */
class ForgotPasswordFragment : BaseFragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Reset button
        binding.btnReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (validateEmail(email)) {
                handlePasswordReset(email)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etEmail.error = getString(R.string.email_required)
                binding.etEmail.requestFocus()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = getString(R.string.invalid_email)
                binding.etEmail.requestFocus()
                false
            }
            else -> {
                binding.etEmail.error = null
                true
            }
        }
    }

    private fun handlePasswordReset(email: String) {
        // Note: Based on the API documentation, there's no specific "forgot password" endpoint
        // In a production app, this would:
        // 1. Send a reset link to the user's email
        // 2. User clicks link and is taken to reset password page
        //
        // For now, we'll show a mock success dialog and guide user to contact admin

        showPasswordResetDialog(email)
    }

    private fun showPasswordResetDialog(email: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.password_reset_requested)
            .setMessage(getString(R.string.password_reset_message, email))
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                // Navigate back to login
                val args = Bundle().apply {
                    putString("user_email", email)
                    putString("reset_token", "mock_token")
                }
                navigateTo(R.id.action_forgotPasswordFragment_to_resetPasswordFragment,args)

              //  findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }

    private fun observeViewModel() {
        // Observe forgot password state when backend endpoint is available
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.forgotPasswordState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading(getString(R.string.sending_reset_email))
                        disableButton()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        enableButton()
                        showSuccess(getString(R.string.reset_email_sent))

                        // Navigate back after delay
                        view?.postDelayed({
                            findNavController().navigateUp()
                        }, 2000)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        enableButton()
                        showError(getString(R.string.reset_email_error, resource.exception.message))
                    }
                    Resource.None -> {
                        hideLoading()
                        enableButton()
                    }
                }
            }
        }
    }

    private fun disableButton() {
        binding.btnReset.isEnabled = false
        binding.btnReset.alpha = 0.5f
    }

    private fun enableButton() {
        binding.btnReset.isEnabled = true
        binding.btnReset.alpha = 1.0f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.resetForgotPasswordState()
        _binding = null
    }
}