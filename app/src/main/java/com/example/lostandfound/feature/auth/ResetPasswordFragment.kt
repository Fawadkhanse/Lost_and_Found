// app/src/main/java/com/example/lostandfound/feature/auth/ResetPasswordFragment.kt
package com.example.lostandfound.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentResetPasswordBinding
import com.example.lostandfound.feature.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * ResetPasswordFragment - Reset password with token
 * This would be used when user clicks reset link from email
 * Token would be passed as navigation argument
 */
class ResetPasswordFragment : BaseFragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    // Token and email from navigation arguments or deep link
    private var resetToken: String? = null
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            resetToken = it.getString("reset_token")
            userEmail = it.getString("user_email")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Pre-fill email if available
        userEmail?.let {
            binding.etEmail.setText(it)
            binding.etEmail.isEnabled = false
        }

        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Reset Password button
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validateInputs(email, newPassword, confirmPassword)) {
                performPasswordReset(email, newPassword, confirmPassword)
            }
        }
    }

    private fun validateInputs(
        email: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = getString(R.string.email_required)
                binding.etEmail.requestFocus()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = getString(R.string.invalid_email)
                binding.etEmail.requestFocus()
                false
            }
            newPassword.isEmpty() -> {
                binding.tilNewPassword.error = getString(R.string.new_password_required)
                binding.etNewPassword.requestFocus()
                false
            }
            newPassword.length < 8 -> {
                binding.tilNewPassword.error = getString(R.string.password_min_length)
                binding.etNewPassword.requestFocus()
                false
            }
            confirmPassword.isEmpty() -> {
                binding.tilConfirmPassword.error = getString(R.string.confirm_password_required)
                binding.etConfirmPassword.requestFocus()
                false
            }
            newPassword != confirmPassword -> {
                binding.tilConfirmPassword.error = getString(R.string.passwords_dont_match)
                binding.etConfirmPassword.requestFocus()
                false
            }
            else -> {
                // Clear all errors
                clearErrors()
                true
            }
        }
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilNewPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun performPasswordReset(
        email: String,
        newPassword: String,
        confirmPassword: String
    ) {


        authViewModel.resetPassword(
            email = email,
            token = resetToken?:"",
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.resetPasswordState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading(getString(R.string.resetting_password))
                        disableButton()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        enableButton()
                        showSuccessDialog()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        enableButton()
                        showError(getString(R.string.password_reset_error, resource.exception.message))
                    }
                    Resource.None -> {
                        hideLoading()
                        enableButton()
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.password_reset_success)
            .setMessage(R.string.password_reset_success_message)
            .setPositiveButton(R.string.login_now) { dialog, _ ->
                dialog.dismiss()
                navigateToLogin()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToLogin() {
        findNavController().navigate(
            R.id.loginFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.resetPasswordFragment, true)
                .build()
        )
    }

    private fun disableButton() {
        binding.btnResetPassword.isEnabled = false
        binding.btnResetPassword.alpha = 0.5f
    }

    private fun enableButton() {
        binding.btnResetPassword.isEnabled = true
        binding.btnResetPassword.alpha = 1.0f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.resetResetPasswordState()
        _binding = null
    }
}