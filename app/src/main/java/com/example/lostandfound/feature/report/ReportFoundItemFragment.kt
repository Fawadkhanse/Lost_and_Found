package com.example.lostandfound.feature.report

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentReportFoundItemBinding
import com.example.lostandfound.domain.auth.CategoryResponse
import com.example.lostandfound.domain.item.FoundItemRequest
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.category.CategoryViewModel
import com.example.lostandfound.feature.item.ItemViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ReportFoundItemFragment - Report a found item
 */
class ReportFoundItemFragment : BaseFragment() {

    private var _binding: FragmentReportFoundItemBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()

    private var categories: List<CategoryResponse> = emptyList()
    private var selectedCategoryId: Int = 0
    private var selectedImageUri: Uri? = null
    private var selectedDate: String = ""
    private var selectedTime: String = "00:00:00"

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportFoundItemBinding.inflate(inflater, container, false)
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
            openImagePicker()
        }

        // Date picker
        binding.etDateFound.setOnClickListener {
            showDatePicker()
        }

        // Time picker
        binding.etTimeFound?.setOnClickListener {
            showTimePicker()
        }

        // Location finder
        binding.btnFindLocation.setOnClickListener {
            Toast.makeText(requireContext(), "Location picker coming soon", Toast.LENGTH_SHORT).show()
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            submitFoundItem()
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
            .centerCrop()
            .into(binding.ivItemImage)

        // Hide the scan text
        binding.tvScanItem.visibility = View.GONE
        binding.ivCamera.visibility = View.GONE
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
                binding.etDateFound.setText(displayFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d:00", hourOfDay, minute)

                // Display format
                binding.etTimeFound?.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24 hour format
        )
        timePickerDialog.show()
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
                        setupCategorySpinner()
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }

        // Observe found item creation
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.createFoundItemState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Submitting found item...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        Toast.makeText(
                            requireContext(),
                            "Found item reported successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        itemViewModel.resetCreateFoundItemState()
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

    private fun submitFoundItem() {
        val itemName = binding.etItemName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val storageLocation = binding.etStorageLocation?.text.toString().trim().ifEmpty { location }
        val categoryPosition = binding.spinnerCategory.selectedItemPosition

        // Validation
        if (itemName.isEmpty()) {
            binding.etItemName.error = "Item name is required"
            return
        }

//        if (categoryPosition == 0 || categories.isEmpty()) {
//            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
//            return
//        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please select date found", Toast.LENGTH_SHORT).show()
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
        if (categories.isNotEmpty()) {
            selectedCategoryId = categories[categoryPosition - 1].id
        }

        // Create request
        val request = FoundItemRequest(
            title = itemName,
            description = description,
            category = selectedCategoryId,
            foundLocation = location,
            foundDate = selectedDate,
            foundTime = selectedTime,
            brand = "", // Optional
            color = "", // Optional
            size = "", // Optional
            searchTags = itemName.lowercase(),
            colorTags = "",
            materialTags = "",
            storageLocation = storageLocation,
            status = "found",
            imageUrl = selectedImageUri?.toString()
        )

        itemViewModel.createFoundItem(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}