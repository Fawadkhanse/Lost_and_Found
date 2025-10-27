package com.example.lostandfound.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lostandfound.databinding.FragmentAddAccountBinding

class AddAccountFragment : Fragment() {

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.ivProfilePhoto.setOnClickListener {
            // Open image picker
        }

        binding.btnCreate.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val userId = binding.etUserId.text.toString()
        val password = binding.etPassword.text.toString()
        val phone = binding.etPhone.text.toString()
        val email = binding.etEmail.text.toString()
        val faculty = binding.etFaculty.text.toString()
        val address = binding.etAddress.text.toString()

        // Validate inputs
        if (userId.isEmpty() || password.isEmpty()) {
            // Show error
            return
        }

        // Create account logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}