package com.example.lostandfound.feature.report


import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.domain.auth.CategoryResponse
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.category.CategoryViewModel
import com.example.lostandfound.feature.item.ItemViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Base Fragment for Report Lost and Found Items
 * Contains shared functionality for both report types
 */
abstract class BaseReportItemFragment : BaseFragment() {

    protected val itemViewModel: ItemViewModel by viewModel()
    protected val categoryViewModel: CategoryViewModel by viewModel()

    protected var categories: List<CategoryResponse> = emptyList()
    protected var selectedCategoryId: Int = 0
    protected var selectedImageUri: Uri? = null
    protected var selectedImageFile: File? = null
    protected var selectedDate: String = ""
    protected var selectedTime: String = "00:00:00"

    // Image picker launcher
    protected val imagePickerLauncher = registerForActivityResult(
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCategories()
        loadCategories()
    }

    /**
     * Open image picker
     */
    protected fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    /**
     * Display selected image - must be implemented by child
     */
    protected abstract fun displaySelectedImage(uri: Uri)

    /**
     * Create file from URI with compression
     */
    protected fun createFileFromUri(uri: Uri): File {
        val file = File(requireContext().cacheDir, "item_image_${System.currentTimeMillis()}.jpg")

        try {
            // Load bitmap from URI
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

            // Calculate scaled dimensions (max 1024px on longest side)
            val maxDimension = 1024
            val scale = Math.min(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height
            )

            val scaledBitmap = if (scale < 1) {
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            // Compress and save to file (80% quality)
            FileOutputStream(file).use { output ->
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, output)
            }

            // Recycle bitmaps to free memory
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to original method if compression fails
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }

        return file
    }

    /**
     * Show date picker dialog
     */
    protected fun showDatePicker(onDateSelected: (String, String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.format(calendar.time)

                // Display format
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val displayDate = displayFormat.format(calendar.time)

                onDateSelected(selectedDate, displayDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    /**
     * Show time picker dialog
     */
    protected fun showTimePicker(onTimeSelected: (String, String) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d:00", hourOfDay, minute)
                val displayTime = String.format("%02d:%02d", hourOfDay, minute)

                onTimeSelected(selectedTime, displayTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24 hour format
        )
        timePickerDialog.show()
    }

    /**
     * Load categories
     */
    protected fun loadCategories() {
        categoryViewModel.getAllCategories()
    }

    /**
     * Observe categories
     */
    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.categoriesListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading categories...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        categories = resource.data.results
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
    }

    /**
     * Setup category spinner - must be implemented by child
     */
    protected abstract fun setupCategorySpinner()

    /**
     * Get category spinner adapter
     */
    protected fun getCategoryAdapter(): ArrayAdapter<String> {
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
        return adapter as ArrayAdapter<String>
    }

    /**
     * Validate common fields
     */
    protected fun validateCommonFields(
        itemName: String,
        categoryPosition: Int,
        location: String,
        description: String
    ): Boolean {
        return when {
            itemName.isEmpty() -> {
                showError("Item name is required")
                false
            }
            categoryPosition == 0 -> {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                false
            }
            selectedDate.isEmpty() -> {
                Toast.makeText(requireContext(), "Please select date", Toast.LENGTH_SHORT).show()
                false
            }
            location.isEmpty() -> {
                showError("Location is required")
                false
            }
            description.isEmpty() -> {
                showError("Description is required")
                false
            }
            else -> true
        }
    }

    /**
     * Get selected category ID
     */
    protected fun getSelectedCategoryId(position: Int): Int {
        return if (categories.isNotEmpty() && position > 0) {
            categories[position - 1].id?:0
        } else {
            1 // Default category
        }
    }
}