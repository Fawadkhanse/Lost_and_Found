package com.example.lostandfound.feature.report

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentReportLostItemBinding
import com.example.lostandfound.domain.auth.CategoryResponse
import com.example.lostandfound.domain.auth.LostItemRequest
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.category.CategoryViewModel
import com.example.lostandfound.feature.item.ItemViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class ReportLostItemFragment : BaseFragment() {

    private var _binding: FragmentReportLostItemBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()

    private var categories: List<CategoryResponse> = emptyList()
    private var selectedCategoryId: Int = 0
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportLostItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModels()
        loadCategories()
    }

    private fun setupViews() {
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Attach image
        binding.cardScanItem.setOnClickListener {
            // TODO: Implement image picker
            Toast.makeText(requireContext(), "Image picker coming soon", Toast.LENGTH_SHORT).show()
        }

        // Date picker
        binding.etDateLost.setOnClickListener {
            showDatePicker()
        }

        // Location finder
        binding.btnFindLocation.setOnClickListener {
            // TODO: Implement location picker
            Toast.makeText(requireContext(), "Location picker coming soon", Toast.LENGTH_SHORT).show()
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            submitLostItem()
        }

        // Bottom navigation

    }


    private fun loadCategories() {
        categoryViewModel.getAllCategories()
    }

    private fun observeViewModels() {
        // Observe categories
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.categoriesListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading categories...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        categories = resource.data
                        setupCategorySpinner()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load categories: ${resource.exception.message}")
                        setupCategorySpinner() // Setup with empty list
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }

        // Observe lost item creation
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.createLostItemState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Submitting lost item...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        Toast.makeText(
                            requireContext(),
                            "Lost item reported successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        itemViewModel.resetCreateLostItemState()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to submit: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun setupCategorySpinner() {
        val categoryNames = if (categories.isEmpty()) {
            listOf("Select Category")
        } else {
            listOf("Select Category") + categories.map { it.name }
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.format(calendar.time)

                // Display format
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etDateLost.setText(displayFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun submitLostItem() {
        val itemName = binding.etItemName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val categoryPosition = binding.spinnerCategory.selectedItemPosition

        // Validation
        if (itemName.isEmpty()) {
            binding.etItemName.error = "Item name is required"
            return
        }

        if (categoryPosition == 0 || categories.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please select date lost", Toast.LENGTH_SHORT).show()
            return
        }

        if (location.isEmpty()) {
            binding.etLocation.error = "Location is required"
            return
        }

        if (description.isEmpty()) {
            binding.etDescription.error = "Description is required"
            return
        }

        // Get selected category
        selectedCategoryId = categories[categoryPosition - 1].id

        // Create request
        val request = LostItemRequest(
            title = itemName,
            description = description,
            category = selectedCategoryId,
            lostLocation = location,
            lostDate = selectedDate,
            lostTime = "00:00:00", // Default time
            brand = "", // Optional
            color = "", // Optional
            size = "", // Optional
            searchTags = itemName.lowercase(),
            colorTags = "",
            materialTags = "",
            status = "lost",
            isVerified = false,
            itemImage = null // TODO: Implement image upload
        )

        itemViewModel.createLostItem(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}