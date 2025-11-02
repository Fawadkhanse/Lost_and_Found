package com.example.lostandfound.feature.claimitem

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
import com.example.lostandfound.databinding.FragmentClaimBinding
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.claimitem.ClaimViewModel
import com.example.lostandfound.feature.item.ItemViewModel
import com.example.lostandfound.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

/**
 * ClaimFragment - Submit a claim for a found item
 * Enhanced with image compression and optimization
 */
class ClaimFragment : BaseFragment() {

    private var _binding: FragmentClaimBinding? = null
    private val binding get() = _binding!!

    private val claimViewModel: ClaimViewModel by viewModel()
    private val itemViewModel: ItemViewModel by viewModel()

    // Item details
    private var foundItemId: String? = null
    private var foundItemTitle: String? = null
    private var foundItemImage: String? = null

    // Selected proof images
    private val selectedProofImages = mutableListOf<Uri>()
    private val selectedProofFiles = mutableListOf<File>()

    // Image picker launchers
    private val proofImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    private val multipleImagesPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.clipData?.let { clipData ->
                val uris = mutableListOf<Uri>()
                for (i in 0 until clipData.itemCount) {
                    uris.add(clipData.getItemAt(i).uri)
                }
                handleMultipleImageSelection(uris)
            } ?: result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            foundItemId = it.getString(ARG_FOUND_ITEM_ID)
            foundItemTitle = it.getString(ARG_FOUND_ITEM_TITLE)
            foundItemImage = it.getString(ARG_FOUND_ITEM_IMAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClaimBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        observeViewModel()
        loadFoundItemDetails()
    }

    private fun setupViews() {
        binding.tvTitle.text = "Claim Found Item"

        foundItemTitle?.let {
            binding.tvFoundItemTitle.text = it
        }

        foundItemImage?.let { imageUrl ->
            if (imageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(binding.ivFoundItemImage)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAddProofImage.setOnClickListener {
            openProofImagePicker()
        }

        binding.btnAddMultipleImages.setOnClickListener {
            openMultipleImagesPicker()
        }

        binding.btnRemoveProofImage.setOnClickListener {
            removeLastProofImage()
        }

        binding.btnSubmitClaim.setOnClickListener {
            submitClaim()
        }

        binding.btnCancel.setOnClickListener {
            showCancelConfirmation()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            claimViewModel.createClaimState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Submitting claim...")
                        disableButtons()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        enableButtons()
                        showSuccess("Claim submitted successfully! Admin will review your claim.")
                        claimViewModel.resetCreateState()

                        // Clean up temporary files
                        ImageUtils.cleanupTempFiles(requireContext())

                        view?.postDelayed({
                            findNavController().navigateUp()
                        }, 1500)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        enableButtons()
                        showError("Failed to submit claim: ${resource.exception.message}")
                        claimViewModel.resetCreateState()
                    }
                    Resource.None -> {
                        hideLoading()
                        enableButtons()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.foundItemDetailState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val item = resource.data
                        binding.tvFoundItemTitle.text = item.title
                        binding.tvFoundItemDescription.text = item.description
                        binding.tvFoundLocation.text = "Found at: ${item.foundLocation}"
                        binding.tvFoundDate.text = "Found on: ${item.foundDate}"

                        val imageUrl = item.imageUrl ?: item.itemImage
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_placeholder)
                                .error(R.drawable.ic_placeholder)
                                .centerCrop()
                                .into(binding.ivFoundItemImage)
                        }
                    }
                    is Resource.Error -> {
                        showError("Failed to load item details")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadFoundItemDetails() {
        foundItemId?.let { id ->
            itemViewModel.getFoundItemById(id)
        }
    }

    private fun openProofImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        proofImagePickerLauncher.launch(intent)
    }

    private fun openMultipleImagesPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        multipleImagesPickerLauncher.launch(intent)
    }

    /**
     * Handle single image selection with compression
     */
    private fun handleImageSelection(uri: Uri) {
        if (selectedProofImages.size >= MAX_PROOF_IMAGES) {
            showError("Maximum $MAX_PROOF_IMAGES images allowed")
            return
        }

        showLoading("Processing image...")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val compressedFile = withContext(Dispatchers.IO) {
                    ImageUtils.createCompressedFileFromUri(
                        requireContext(),
                        uri,
                        maxWidth = 1024,
                        maxHeight = 1024,
                        quality = 80
                    )
                }

                hideLoading()

                if (compressedFile != null) {
                    selectedProofImages.add(uri)
                    selectedProofFiles.add(compressedFile)
                    updateProofImagesDisplay()

                    val sizeInfo = ImageUtils.formatFileSize(compressedFile)
                    showSuccess("Image added (${selectedProofImages.size}/$MAX_PROOF_IMAGES) - $sizeInfo")
                } else {
                    showError("Failed to process image")
                }
            } catch (e: Exception) {
                hideLoading()
                showError("Error processing image: ${e.message}")
            }
        }
    }

    /**
     * Handle multiple image selection with compression
     */
    private fun handleMultipleImageSelection(uris: List<Uri>) {
        val remainingSlots = MAX_PROOF_IMAGES - selectedProofImages.size
        if (uris.size > remainingSlots) {
            showError("Can only add $remainingSlots more image(s)")
            return
        }

        showLoading("Processing ${uris.size} image(s)...")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var successCount = 0

                uris.forEach { uri ->
                    val compressedFile = withContext(Dispatchers.IO) {
                        ImageUtils.createCompressedFileFromUri(
                            requireContext(),
                            uri,
                            maxWidth = 1024,
                            maxHeight = 1024,
                            quality = 80
                        )
                    }

                    if (compressedFile != null) {
                        selectedProofImages.add(uri)
                        selectedProofFiles.add(compressedFile)
                        successCount++
                    }
                }

                hideLoading()
                updateProofImagesDisplay()

                if (successCount == uris.size) {
                    showSuccess("Added $successCount image(s) successfully")
                } else {
                    showError("Added $successCount/${uris.size} images. Some failed to process.")
                }
            } catch (e: Exception) {
                hideLoading()
                showError("Error processing images: ${e.message}")
            }
        }
    }

    private fun removeLastProofImage() {
        if (selectedProofImages.isNotEmpty()) {
            val removedFile = selectedProofFiles.removeAt(selectedProofFiles.size - 1)
            selectedProofImages.removeAt(selectedProofImages.size - 1)

            // Delete the compressed file
            try {
                removedFile.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            updateProofImagesDisplay()
            showInfo("Image removed")
        } else {
            showInfo("No images to remove")
        }
    }

    private fun updateProofImagesDisplay() {
        binding.tvProofImageCount.text = "${selectedProofImages.size} image(s) selected"

        if (selectedProofImages.isNotEmpty()) {
            Glide.with(requireContext())
                .load(selectedProofImages[0])
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(binding.ivProofPreview)

            binding.ivProofPreview.visibility = View.VISIBLE
            binding.tvNoProofImage.visibility = View.GONE
            binding.btnRemoveProofImage.visibility = View.VISIBLE

            // Show total size
            val totalSizeKB = selectedProofFiles.sumOf { ImageUtils.getFileSizeInKB(it) }
            val sizeText = if (totalSizeKB < 1024) {
                "$totalSizeKB KB"
            } else {
                String.format("%.2f MB", totalSizeKB / 1024.0)
            }
            binding.tvProofImageCount.text = "${selectedProofImages.size} image(s) - Total: $sizeText"
        } else {
            binding.ivProofPreview.visibility = View.GONE
            binding.tvNoProofImage.visibility = View.VISIBLE
            binding.btnRemoveProofImage.visibility = View.GONE
        }

        binding.btnAddProofImage.text = if (selectedProofImages.isEmpty()) {
            "Add Proof of Ownership"
        } else {
            "Add More Images (${selectedProofImages.size}/$MAX_PROOF_IMAGES)"
        }
    }

    private fun submitClaim() {
        if (!validateInputs()) {
            return
        }

        val claimDescription = binding.etClaimDescription.text.toString().trim()
        val proofOfOwnership = binding.etProofDescription.text.toString().trim()

        // Convert image URIs to comma-separated string
        val supportingImagesUrls = selectedProofImages.joinToString(",") { it.toString() }

        if (foundItemId.isNullOrEmpty()) {
            showError("Found item ID is missing")
            return
        }

        claimViewModel.createClaim(
            foundItem = foundItemId!!,
            claimDescription = claimDescription,
            proofOfOwnership = proofOfOwnership,
            supportingImages = supportingImagesUrls.ifEmpty { null },
            status = "pending",
            adminNotes = null
        )
    }

    private fun validateInputs(): Boolean {
        val claimDescription = binding.etClaimDescription.text.toString().trim()
        val proofDescription = binding.etProofDescription.text.toString().trim()

        return when {
            claimDescription.isEmpty() -> {
                binding.tilClaimDescription.error = "Please describe why this item is yours"
                binding.etClaimDescription.requestFocus()
                false
            }
            claimDescription.length < 20 -> {
                binding.tilClaimDescription.error = "Please provide more details (at least 20 characters)"
                binding.etClaimDescription.requestFocus()
                false
            }
            proofDescription.isEmpty() -> {
                binding.tilProofDescription.error = "Please describe your proof of ownership"
                binding.etProofDescription.requestFocus()
                false
            }
            proofDescription.length < 20 -> {
                binding.tilProofDescription.error = "Please provide more details (at least 20 characters)"
                binding.etProofDescription.requestFocus()
                false
            }
//            selectedProofImages.isEmpty() -> {
//                showError("Please add at least one proof image (receipt, photo, etc.)")
//                false
//            }
            else -> {
                binding.tilClaimDescription.error = null
                binding.tilProofDescription.error = null
                true
            }
        }
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Claim")
            .setMessage("Are you sure you want to cancel? All entered information will be lost.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                // Clean up temporary files
                selectedProofFiles.forEach { it.delete() }
                findNavController().navigateUp()
            }
            .setNegativeButton("No, Continue", null)
            .show()
    }

    private fun disableButtons() {
        binding.btnSubmitClaim.isEnabled = false
        binding.btnCancel.isEnabled = false
        binding.btnAddProofImage.isEnabled = false
        binding.btnAddMultipleImages.isEnabled = false
    }

    private fun enableButtons() {
        binding.btnSubmitClaim.isEnabled = true
        binding.btnCancel.isEnabled = true
        binding.btnAddProofImage.isEnabled = true
        binding.btnAddMultipleImages.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FOUND_ITEM_ID = "foundItemId"
        private const val ARG_FOUND_ITEM_TITLE = "foundItemTitle"
        private const val ARG_FOUND_ITEM_IMAGE = "foundItemImage"
        private const val MAX_PROOF_IMAGES = 5

        fun newInstance(
            foundItemId: String,
            foundItemTitle: String? = null,
            foundItemImage: String? = null
        ): ClaimFragment {
            return ClaimFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FOUND_ITEM_ID, foundItemId)
                    foundItemTitle?.let { putString(ARG_FOUND_ITEM_TITLE, it) }
                    foundItemImage?.let { putString(ARG_FOUND_ITEM_IMAGE, it) }
                }
            }
        }
    }
}