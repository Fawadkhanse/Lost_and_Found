// app/src/main/java/com/example/lostandfound/feature/report/ReportFoundItemFragment.kt
package com.example.lostandfound.feature.report

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentReportFoundItemBinding
import kotlinx.coroutines.launch

class ReportFoundItemFragment : BaseReportItemFragment() {

    private var _binding: FragmentReportFoundItemBinding? = null
    private val binding get() = _binding!!

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
        observeViewModel()
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
            showDatePicker { apiDate, displayDate ->
                binding.etDateFound.setText(displayDate)
            }
        }

        // Time picker
        binding.etTimeFound?.setOnClickListener {
            showTimePicker { apiTime, displayTime ->
                binding.etTimeFound?.setText(displayTime)
            }
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

    override fun displaySelectedImage(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .centerCrop()
            .into(binding.ivItemImage)

        // Hide the scan text
        binding.tvScanItem.visibility = View.GONE
        binding.ivCamera.visibility = View.GONE
    }

    override fun setupCategorySpinner() {
        binding.spinnerCategory.adapter = getCategoryAdapter()
    }

    private fun observeViewModel() {
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

    private fun submitFoundItem() {
        val itemName = binding.etItemName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val storageLocation = binding.etStorageLocation?.text.toString().trim().ifEmpty { location }
        val categoryPosition = binding.spinnerCategory.selectedItemPosition

        // Validation
        if (!validateCommonFields(itemName, categoryPosition, location, description)) {
            return
        }

        // Get selected category
        selectedCategoryId = getSelectedCategoryId(categoryPosition)

        // Submit with multipart image
        itemViewModel.createFoundItemWithImage(
            title = itemName,
            description = description,
            category = selectedCategoryId,
            foundLocation = location,
            foundDate = selectedDate,
            foundTime = selectedTime,
            brand = "",
            color = "",
            size = "",
            searchTags = itemName.lowercase(),
            colorTags = "",
            materialTags = "",
            storageLocation = storageLocation,
            status = "found",
            itemImageFile = selectedImageFile
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}