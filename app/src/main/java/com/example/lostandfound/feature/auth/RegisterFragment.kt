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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentRegisterBinding
import com.example.lostandfound.feature.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class RegisterFragment : BaseFragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
                // Convert URI to File
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
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Profile photo click to select image
        binding.ivProfilePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnCreate.setOnClickListener {
            val userId = binding.etUserId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val faculty = binding.etFaculty.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()

            if (validateInputs(userId, password, phone, email, faculty, address)) {
                authViewModel.registerWithImage(
                    username = userId,
                    email = email,
                    password = password,
                    password2 = password,
                    firstName = "Admin",  // You can add separate fields for these
                    lastName = "Doe",
                   // userType = "resident",
                    userType = "Admin",
                    phoneNumber = phone,
                    towerNumber = address,
                    roomNumber = faculty,
                    profileImageFile = selectedImageFile
                )
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
    }

    private fun createFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
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
                        showError(resource.exception.message)
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
            password.length < 8 -> {
                binding.etPassword.error = "Password must be at least 8 characters"
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
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Invalid email format"
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
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}