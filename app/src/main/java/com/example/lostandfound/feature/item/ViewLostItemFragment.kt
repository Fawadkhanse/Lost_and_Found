package com.example.lostandfound.feature.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.lostandfound.databinding.FragmentViewLostItemBinding

class ViewLostItemFragment : Fragment() {

    private var _binding: FragmentViewLostItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewLostItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // Setup Category Spinner
        val categories = arrayOf("Smartphone", "Keys", "Wallet", "Documents", "Others")
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Setup Status Spinner
        val statuses = arrayOf("Available", "Claimed", "Pending")
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statuses
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter

        // Load item data
        loadItemData()
    }

    private fun loadItemData() {
        // Load from database or arguments
        // For now, using dummy data shown in layouts
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            enableEditing()
        }

        binding.btnDelete.setOnClickListener {
            // Show delete confirmation dialog
            deleteItem()
        }

        // Bottom navigation
        binding.bottomNav.navHome.setOnClickListener {
            // Navigate to home
        }

        binding.bottomNav.navMessage.setOnClickListener {
            // Navigate to messages
        }

        binding.bottomNav.navAccount.setOnClickListener {
            // Navigate to account
        }
    }

    private fun enableEditing() {
        binding.etItemName.isEnabled = true
        binding.spinnerCategory.isEnabled = true
        binding.etLocation.isEnabled = true
        binding.etDescription.isEnabled = true
        binding.spinnerStatus.isEnabled = true
    }

    private fun deleteItem() {
        // Implement delete logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}