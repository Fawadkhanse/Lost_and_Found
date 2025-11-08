package com.example.lostandfound.feature.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentViewItemBinding
import com.example.lostandfound.domain.auth.LostItemResponse
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.claimitem.ClaimViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ViewItemFragment : BaseFragment() {

    private var _binding: FragmentViewItemBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val claimViewModel: ClaimViewModel by viewModel()

    private var itemId: String? = null
    private var itemType: String? = null // "LOST" or "FOUND"
    private var currentItem: LostItemResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getString("itemId")
        itemType = arguments?.getString("itemType")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModels()
        loadItemData()
    }

    private fun loadItemData() {
        if (itemId == null) {
            showError("Item ID not found")
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        // Load based on item type
        when (itemType?.uppercase()) {
            "LOST" -> itemViewModel.getLostItemById(itemId!!)
            "FOUND" -> itemViewModel.getFoundItemById(itemId!!)
            else -> {
                showError("Invalid item type")
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun observeViewModels() {
        // Observe item detail (works for both lost and found items)
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.itemDetailState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading item details...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        currentItem = resource.data
                        displayItemData(resource.data)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load item: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun displayItemData(item: LostItemResponse) {
        binding.tvItemName.text = item.title
        binding.tvCategory.text = item.categoryName

        // Handle date and location based on item type
        if (itemType?.uppercase() == "LOST") {
            binding.tvDateLost.text = item.lostDate ?: "N/A"
            binding.tvLocation.text = item.lostLocation ?: "N/A"
        } else {
            binding.tvDateLost.text = item.foundDate ?: "N/A"
            binding.tvLocation.text = item.foundLocation ?: "N/A"
        }

        // Load image - check both image fields
        val imageUrl = item.imageUrl ?: item.itemImage
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(binding.ivCamera)

            // Hide scan text when image is loaded
            binding.tvScanItem.visibility = View.GONE
            binding.ivCamera.visibility = View.VISIBLE
        } else {
            binding.tvScanItem.visibility = View.VISIBLE
            binding.ivCamera.visibility = View.GONE
        }

        // Update button visibility based on status
        when (item.status.lowercase()) {
            "found", "claimed", "returned" -> {
                binding.btnFound.isEnabled = false
                binding.btnFound.alpha = 0.5f
                binding.btnFound.text = item.status.uppercase()
            }
            else -> {
                binding.btnFound.isEnabled = true
                binding.btnFound.alpha = 1.0f
                binding.btnFound.text = "MARK AS FOUND"
            }
        }
    }

    private fun setupListeners() {
        binding.cardScanItem.setOnClickListener {
            // TODO: Implement image picker
            Toast.makeText(requireContext(), "Image upload coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnFound.setOnClickListener {
            markItemAsFound()
        }

        binding.btnPendingClaims.setOnClickListener {
            viewPendingClaims()
        }
    }

    private fun markItemAsFound() {
        // TODO: Implement update item status API
        Toast.makeText(
            requireContext(),
            "Mark as found feature coming soon",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun viewPendingClaims() {
        itemId?.let { id ->
            // TODO: Navigate to claims list for this item
            Toast.makeText(
                requireContext(),
                "Viewing claims for item: $id",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemViewModel.resetLostItemDetailState()
        _binding = null
    }

    companion object {
        fun newInstance(itemId: String, itemType: String): ViewItemFragment {
            return ViewItemFragment().apply {
                arguments = Bundle().apply {
                    putString("itemId", itemId)
                    putString("itemType", itemType)
                }
            }
        }
    }
}