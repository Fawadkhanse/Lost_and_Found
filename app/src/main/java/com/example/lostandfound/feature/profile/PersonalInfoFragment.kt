// app/src/main/java/com/example/lostandfound/feature/profile/PersonalInfoFragment.kt
package com.example.lostandfound.feature.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentPersonalInfoBinding
import com.example.lostandfound.feature.auth.AuthViewModel
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * PersonalInfoFragment - Enhanced Profile Management
 * Features:
 * - View current user profile with enhanced UI
 * - Update profile information
 * - Change password with dialog
 * - Logout with confirmation
 * - Profile picture selection
 * Works for both Admin and Resident users
 */
class PersonalInfoFragment : BaseFragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()
    private val profileViewModel: ProfileViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var isEditMode = false

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displayProfileImage(uri)
                showInfo("Profile image updated (save to apply changes)")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        observeViewModels()
        loadUserProfile()
    }

    private fun setupViews() {
        // Set read-only mode by default
        setEditMode(false)

        // Hide camera icon initially
        binding.ivCameraIcon.visibility = View.GONE
    }

    private fun setupListeners() {
//        // Back button
//        binding.btnBack.setOnClickListener {
//            if (isEditMode && hasUnsavedChanges()) {
//                showDiscardChangesDialog()
//            } else {
//                requireActivity().onBackPressedDispatcher.onBackPressed()
//            }
//        }



        // Profile photo click - only in edit mode
        binding.cardProfilePhoto.setOnClickListener {
            if (isEditMode) {
                openImagePicker()
            }
        }

        binding.ivCameraIcon.setOnClickListener {
            openImagePicker()
        }

        // Change Password button
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Update Info button - toggles between edit and save
        binding.btnUpdateInfo.setOnClickListener {
            if (isEditMode) {
                // Save mode - validate and update
                saveProfileChanges()
            } else {
                // Edit mode - enable editing
                setEditMode(true)
                binding.btnUpdateInfo.text = getString(R.string.save_changes)
                binding.btnUpdateInfo.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_check, 0, 0, 0
                )
            }
        }

        // Main Logout button
        binding.btnLogoutMain.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun observeViewModels() {
        // Observe current user profile
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.currentUserState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading profile...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        displayUserProfile(resource.data)
                        // Update AuthData
                        AuthData.setCurrentUserResponse(resource.data)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load profile: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }

        // Observe password update
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.updatePasswordState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Updating password...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Password updated successfully!")
                        profileViewModel.resetUpdatePasswordState()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to update password: ${resource.exception.message}")
                        profileViewModel.resetUpdatePasswordState()
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun loadUserProfile() {
        // Try to load from API first
        profileViewModel.getCurrentUser()

        // Also display from AuthData as fallback
        AuthData.userDetailInfo?.let { user ->
            displayProfileImage(user.profileImage)
            binding.etUserId.setText("${user.firstName} ${user.lastName}")
            binding.etEmail.setText(user.email)
            binding.etPhone.setText(user.phoneNumber ?: "")
            binding.etTowerNumber.setText(user.towerNumber ?: "")
            binding.etRoomNumber.setText(user.roomNumber ?: "")

            // Set user type badge
            binding.tvUserTypeBadge.text = user.userType.uppercase()
            when (user.userType.lowercase()) {
                "admin" -> {
                    binding.tvUserTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.blue)
                }
                "resident" -> {
                    binding.tvUserTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.primary_teal)
                }
            }

            // Set account info
            binding.tvAccountId.text = getString(R.string.account_id,
                user.id.takeLast(12).chunked(4).joinToString("-"))

            // Set member since date
//            user.createdAt?.let { dateString ->
//                val memberSince = formatMemberSinceDate(dateString)
//                binding.tvMemberSince.text = getString(R.string.member_since, memberSince)
//            }
        }
    }

    private fun displayUserProfile(user: com.example.lostandfound.domain.auth.CurrentUserResponse) {
        binding.apply {
            // Display profile image
            displayProfileImage(user.profileImageUrl ?: user.profileImage)

            // Display user information
            etUserId.setText("${user.firstName} ${user.lastName}")
            etEmail.setText(user.email)
            etPhone.setText(user.phoneNumber ?: "")
            etTowerNumber.setText(user.towerNumber ?: "")
            etRoomNumber.setText(user.roomNumber ?: "")

            // Set user type badge
            tvUserTypeBadge.text = user.userType.uppercase()
            when (user.userType.lowercase()) {
                "admin" -> {
                    tvUserTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.blue)
                    // For admin, change labels
                    tilTowerNumber.hint = "Department"
                    tilRoomNumber.hint = "Office"
                }
                "resident" -> {
                    tvUserTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.primary_teal)
                }
            }

            // Set account ID
            tvAccountId.text = getString(R.string.account_id,
                user.id.takeLast(12).chunked(4).joinToString("-"))

            // Set member since
            user.createdAt?.let { dateString ->
                val memberSince = formatMemberSinceDate(dateString)
                tvMemberSince.text = getString(R.string.member_since, memberSince)
            }
        }
    }

    private fun displayProfileImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_account)
                .error(R.drawable.ic_account)
                .circleCrop()
                .into(binding.ivProfilePhoto)
        } else {
            binding.ivProfilePhoto.setImageResource(R.drawable.ic_account)
        }
    }

    private fun displayProfileImage(imageUri: Uri) {
        Glide.with(requireContext())
            .load(imageUri)
            .placeholder(R.drawable.ic_account)
            .error(R.drawable.ic_account)
            .circleCrop()
            .into(binding.ivProfilePhoto)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled

        binding.apply {
            // Enable/disable fields
            etEmail.isEnabled = enabled
            etPhone.isEnabled = enabled

            // User ID, Tower, Room are always disabled (read-only)
            etUserId.isEnabled = false
            etTowerNumber.isEnabled = false
            etRoomNumber.isEnabled = false

            // Show/hide camera icon
            ivCameraIcon.visibility = if (enabled) View.VISIBLE else View.GONE

            // Update profile photo text
            tvProfilePhoto.text = if (enabled) {
                getString(R.string.tap_to_change_photo)
            } else {
                getString(R.string.profile_photo)
            }
        }
    }

    private fun saveProfileChanges() {
        // Get values
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        // Validate
        if (!validateProfileInput(phone, email)) {
            return
        }

        // Show confirmation
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_update_title)
            .setMessage(R.string.confirm_update_message)
            .setPositiveButton("Update") { _, _ ->
                performProfileUpdate(phone, email)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateProfileInput(phone: String, email: String): Boolean {
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
            phone.isNotEmpty() && phone.length < 10 -> {
                binding.tilPhone.error = getString(R.string.invalid_phone)
                binding.etPhone.requestFocus()
                false
            }
            else -> {
                // Clear errors
                binding.tilEmail.error = null
                binding.tilPhone.error = null
                true
            }
        }
    }

    private fun performProfileUpdate(phone: String, email: String) {
        // Note: Based on API docs, there's no direct profile update endpoint
        // Only password update is available

        showInfo("Profile update feature coming soon. Only password can be changed currently.")

        // Reset to read-only mode
        setEditMode(false)
        binding.btnUpdateInfo.text = getString(R.string.edit_profile)
        binding.btnUpdateInfo.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_edit, 0, 0, 0
        )
    }

    private fun showChangePasswordDialog(newPassword: String? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etConfirmPassword)

        // Pre-fill new password if provided
        newPassword?.let {
            etNewPassword.setText(it)
            etConfirmPassword.setText(it)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.change_password)
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val oldPass = etOldPassword.text.toString()
                val newPass = etNewPassword.text.toString()
                val confirmPass = etConfirmPassword.text.toString()

                if (validatePasswordChange(oldPass, newPass, confirmPass)) {
                    profileViewModel.updatePassword(oldPass, newPass, confirmPass)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validatePasswordChange(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        return when {
            oldPassword.isEmpty() -> {
                showError(getString(R.string.current_password_required))
                false
            }
            newPassword.isEmpty() -> {
                showError(getString(R.string.new_password_required))
                false
            }
            newPassword.length < 8 -> {
                showError(getString(R.string.password_min_length))
                false
            }
            newPassword != confirmPassword -> {
                showError(getString(R.string.passwords_dont_match))
                false
            }
            oldPassword == newPassword -> {
                showError(getString(R.string.password_must_differ))
                false
            }
            else -> true
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_logout_title)
            .setMessage(R.string.confirm_logout_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun performLogout() {
        // Clear auth data
        authViewModel.logout()
        AuthData.clearAuthData()

        // Navigate to login and clear back stack
        try {
            findNavController().navigate(
                R.id.action_personalInfoFragment_to_loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.personalInfoFragment, true)
                    .build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: just navigate to login
            try {
                findNavController().navigate(R.id.loginFragment)
            } catch (e: Exception) {
                // If navigation fails, finish activity
                requireActivity().finish()
            }
        }

        showSuccess(getString(R.string.logged_out))
    }

    private fun hasUnsavedChanges(): Boolean {
        // Check if current values differ from original
        AuthData.userDetailInfo?.let { original ->
            val currentEmail = binding.etEmail.text.toString().trim()
            val currentPhone = binding.etPhone.text.toString().trim()

            return currentEmail != original.email ||
                    currentPhone != (original.phoneNumber ?: "")
        }
        return false
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.discard_changes)
            .setMessage(R.string.unsaved_changes)
            .setPositiveButton(R.string.discard) { _, _ ->
                // Discard changes and go back
                setEditMode(false)
                loadUserProfile() // Reload original data
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setNegativeButton(R.string.keep_editing, null)
            .show()
    }

    private fun formatMemberSinceDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            try {
                // Try simpler format
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}