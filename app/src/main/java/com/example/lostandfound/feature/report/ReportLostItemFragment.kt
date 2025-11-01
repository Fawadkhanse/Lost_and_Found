// app/src/main/java/com/example/lostandfound/feature/report/ReportLostItemFragment.kt
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
import com.example.lostandfound.databinding.FragmentReportLostItemBinding
import kotlinx.coroutines.launch

class ReportLostItemFragment : BaseReportItemFragment() {

    private var _binding: FragmentReportLostItemBinding? = null
    private val binding get() = _binding!!

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
        binding.etDateLost.setOnClickListener {
            showDatePicker { apiDate, displayDate ->
                binding.etDateLost.setText(displayDate)
            }
        }

        // Location finder
        binding.btnFindLocation.setOnClickListener {
            Toast.makeText(requireContext(), "Location picker coming soon", Toast.LENGTH_SHORT).show()
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            submitLostItem()
        }
    }

    override fun displaySelectedImage(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .centerCrop()
            .into(binding.ivCamera)

        // Hide the scan text
        binding.tvScanItem.visibility = View.GONE
    }

    override fun setupCategorySpinner() {
        binding.spinnerCategory.adapter = getCategoryAdapter()
    }

    private fun observeViewModel() {
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

    private fun submitLostItem() {
        val itemName = binding.etItemName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val categoryPosition = binding.spinnerCategory.selectedItemPosition

        // Validation
        if (!validateCommonFields(itemName, categoryPosition, location, description)) {
            return
        }

        // Get selected category
        selectedCategoryId = getSelectedCategoryId(categoryPosition)

        // Submit with multipart image
        itemViewModel.createLostItemWithImage(
            title = itemName,
            description = description,
            category = selectedCategoryId,
            lostLocation = location,
            lostDate = selectedDate,
            lostTime = selectedTime,
            brand = "",
            color = "",
            size = "",
            searchTags = itemName.lowercase(),
            colorTags = "",
            materialTags = "",
            status = "lost",
            isVerified = false,
            itemImageFile = selectedImageFile
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}