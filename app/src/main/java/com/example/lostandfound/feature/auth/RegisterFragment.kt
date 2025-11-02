// app/src/main/java/com/example/lostandfound/feature/auth/RegisterFragment.kt
package com.example.lostandfound.feature.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource

import com.example.lostandfound.databinding.FragmentRegisterBinding
import com.example.lostandfound.feature.auth.AuthViewModel
import com.example.lostandfound.feature.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

/**
 * AddUserFragment - Admin can register new users (residents/admins)
 * Enhanced with better UI and user type selection
 */
class RegisterFragment : BaseFragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null
    private var selectedUserType: String = "resident" // Default

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
                selectedImageFile = createFileFromUri(uri)
            }
        }
    }

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
        // Setup user type spinner
        val userTypes = arrayOf("Resident", "Admin")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUserType.adapter = adapter

        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Profile photo selection
        binding.ivProfilePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnSelectPhoto.setOnClickListener {
            openImagePicker()
        }

        // User type selection
        binding.spinnerUserType.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUserType = when (position) {
                    0 -> "resident"
                    1 -> "admin"
                    else -> "resident"
                }
                updateUIForUserType()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedUserType = "resident"
            }
        })

        // Create button
        binding.btnCreate.setOnClickListener {
            registerNewUser()
        }

        // Cancel button
        binding.btnCancel.setOnClickListener {
            showCancelConfirmation()
        }
    }

    private fun updateUIForUserType() {
        // You can customize fields based on user type
        when (selectedUserType) {
            "admin" -> {
                binding.tilTowerNumber.hint = "Department"
                binding.tilRoomNumber.hint = "Office Number"
            }
            "resident" -> {
                binding.tilTowerNumber.hint = "Tower Number"
                binding.tilRoomNumber.hint = "Room Number"
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun displaySelectedImage(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .placeholder(R.drawable.ic_account)
            .error(R.drawable.ic_account)
            .circleCrop()
            .into(binding.ivProfilePhoto)

        binding.tvPhotoHint.visibility = View.GONE
    }

    private fun createFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.jpg")

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    private fun registerNewUser() {
        // Get input values
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val towerNumber = binding.etTowerNumber.text.toString().trim()
        val roomNumber = binding.etRoomNumber.text.toString().trim()

        // Validate inputs
        if (!validateInputs(
                firstName, lastName, username, email,
                password, confirmPassword, phoneNumber,
                towerNumber, roomNumber
            )) {
            return
        }

        // Register user
        authViewModel.registerWithImage(
            username = username,
            email = email,
            password = password,
            password2 = confirmPassword,
            firstName = firstName,
            lastName = lastName,
            userType = selectedUserType,
            phoneNumber = phoneNumber,
            towerNumber = towerNumber,
            roomNumber = roomNumber,
            profileImageFile = selectedImageFile
        )
    }

    private fun validateInputs(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String,
        towerNumber: String,
        roomNumber: String
    ): Boolean {
        return when {
            firstName.isEmpty() -> {
                binding.tilFirstName.error = "First name is required"
                binding.etFirstName.requestFocus()
                false
            }
            lastName.isEmpty() -> {
                binding.tilLastName.error = "Last name is required"
                binding.etLastName.requestFocus()
                false
            }
            username.isEmpty() -> {
                binding.tilUsername.error = "Username is required"
                binding.etUsername.requestFocus()
                false
            }
            username.length < 3 -> {
                binding.tilUsername.error = "Username must be at least 3 characters"
                binding.etUsername.requestFocus()
                false
            }
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"
                binding.etEmail.requestFocus()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Invalid email format"
                binding.etEmail.requestFocus()
                false
            }
            password.isEmpty() -> {
                binding.tilPassword.error = "Password is required"
                binding.etPassword.requestFocus()
                false
            }
            password.length < 8 -> {
                binding.tilPassword.error = "Password must be at least 8 characters"
                binding.etPassword.requestFocus()
                false
            }
            confirmPassword.isEmpty() -> {
                binding.tilConfirmPassword.error = "Please confirm password"
                binding.etConfirmPassword.requestFocus()
                false
            }
            password != confirmPassword -> {
                binding.tilConfirmPassword.error = "Passwords do not match"
                binding.etConfirmPassword.requestFocus()
                false
            }
            phoneNumber.isEmpty() -> {
                binding.tilPhoneNumber.error = "Phone number is required"
                binding.etPhoneNumber.requestFocus()
                false
            }
            phoneNumber.length < 10 -> {
                binding.tilPhoneNumber.error = "Invalid phone number"
                binding.etPhoneNumber.requestFocus()
                false
            }
            towerNumber.isEmpty() -> {
                binding.tilTowerNumber.error = "${binding.tilTowerNumber.hint} is required"
                binding.etTowerNumber.requestFocus()
                false
            }
            roomNumber.isEmpty() -> {
                binding.tilRoomNumber.error = "${binding.tilRoomNumber.hint} is required"
                binding.etRoomNumber.requestFocus()
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
        binding.tilFirstName.error = null
        binding.tilLastName.error = null
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tilPhoneNumber.error = null
        binding.tilTowerNumber.error = null
        binding.tilRoomNumber.error = null
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.registerState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Creating user account...")
                        disableButtons()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        enableButtons()
                        showSuccess("User registered successfully!")

                        // Navigate back after delay
                        view?.postDelayed({
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }, 1500)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        enableButtons()
                        showError("Registration failed: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                        enableButtons()
                    }
                }
            }
        }
    }

    private fun showCancelConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Registration")
            .setMessage("Are you sure you want to cancel? All entered information will be lost.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setNegativeButton("No, Continue", null)
            .show()
    }

    private fun disableButtons() {
        binding.btnCreate.isEnabled = false
        binding.btnCancel.isEnabled = false
        binding.btnSelectPhoto.isEnabled = false
        binding.spinnerUserType.isEnabled = false
    }

    private fun enableButtons() {
        binding.btnCreate.isEnabled = true
        binding.btnCancel.isEnabled = true
        binding.btnSelectPhoto.isEnabled = true
        binding.spinnerUserType.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}