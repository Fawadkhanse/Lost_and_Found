package com.example.lostandfound.feature.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentItemDetailBinding
import com.example.lostandfound.domain.auth.LostItemResponse
import com.example.lostandfound.domain.item.FoundItemResponse
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.chat.SendMessageDialogFragment
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ItemDetailFragment - Display detailed information about Lost or Found items
 * Supports viewing item details and claiming found items
 * Enhanced with contact owner functionality
 */
class ItemDetailFragment : BaseFragment() {

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val claimViewModel: ClaimViewModel by viewModel()

    private var itemId: String? = null
    private var itemType: ItemType = ItemType.LOST
    private var currentLostItem: LostItemResponse? = null
    private var currentFoundItem: FoundItemResponse? = null

    enum class ItemType {
        LOST, FOUND
    }
    private  val ARG_ITEM_ID = "itemId"
    private  val ARG_ITEM_TYPE = "itemType"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemId = it.getString(ARG_ITEM_ID)
            itemType = ItemType.valueOf(it.getString(ARG_ITEM_TYPE, ItemType.LOST.name))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModels()
        loadItemData()
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Claim button (for found items)
        binding.btnClaimItem.setOnClickListener {
            showClaimDialog()
        }

        // View claims button (for lost items with admin access)
        binding.btnViewClaims.setOnClickListener {
            viewClaims()
        }

        // Contact button - Opens message dialog
        binding.btnContact.setOnClickListener {
            contactItemOwner()
        }

    }


    private fun loadItemData() {
        itemId?.let { id ->
            when (itemType) {
                ItemType.LOST -> itemViewModel.getLostItemById(id)
                ItemType.FOUND -> itemViewModel.getFoundItemById(id)
            }
        } ?: run {
            showError("Item ID not found")
            // requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModels() {
        // Observe Lost Item Detail
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.lostItemDetailState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Loading item details...")
                    is Resource.Success -> {
                        hideLoading()
                        currentLostItem = resource.data
                        displayLostItemData(resource.data)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load item: ${resource.exception.message}")
                    }
                    Resource.None -> hideLoading()
                }
            }
        }

        // Observe Found Item Detail
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.foundItemDetailState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Loading item details...")
                    is Resource.Success -> {
                        hideLoading()
                        currentFoundItem = resource.data
                        displayFoundItemData(resource.data)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load item: ${resource.exception.message}")
                    }
                    Resource.None -> hideLoading()
                }
            }
        }

        // Observe Claim Creation
        viewLifecycleOwner.lifecycleScope.launch {
            claimViewModel.createClaimState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Submitting claim...")
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Claim submitted successfully!")
                        claimViewModel.resetCreateState()
                        // Refresh item data
                        loadItemData()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to submit claim: ${resource.exception.message}")
                    }
                    Resource.None -> hideLoading()
                }
            }
        }
    }

    private fun displayLostItemData(item: LostItemResponse) {
        binding.apply {
            // Set title
            tvTitle.text = "Lost Item Details"

            // Item information
            tvItemName.text = item.title
            tvCategory.text = item.categoryName
            tvDateValue.text = formatDate(item.lostDate)
            tvTimeValue.text = item.lostTime
            tvLocationValue.text = item.lostLocation
            tvDescription.text = item.description

            // Additional details
            if (item.brand.isNotEmpty()) {
                tvBrandLabel.visibility = View.VISIBLE
                tvBrandValue.visibility = View.VISIBLE
                tvBrandValue.text = item.brand
            }

            if (item.color.isNotEmpty()) {
                tvColorLabel.visibility = View.VISIBLE
                tvColorValue.visibility = View.VISIBLE
                tvColorValue.text = item.color
            }

            if (item.size.isNotEmpty()) {
                tvSizeLabel.visibility = View.VISIBLE
                tvSizeValue.visibility = View.VISIBLE
                tvSizeValue.text = item.size
            }

            // Status badge
            tvStatus.text = item.status.uppercase()
            updateStatusBadge(item.status)

            // Tags
            if (item.searchTagsList.isNotEmpty()) {
                tvTagsLabel.visibility = View.VISIBLE
                tvTagsValue.visibility = View.VISIBLE
                tvTagsValue.text = item.searchTagsList.joinToString(", ")
            }

            // Load image
            if (!item.itemImage.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(item.itemImage)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(ivItemImage)
            }

            // Button visibility for lost items
            btnClaimItem.visibility = View.GONE
            btnViewClaims.visibility = if (AuthData.userDetailInfo?.userType == "admin") {
                View.VISIBLE
            } else {
                View.GONE
            }
            btnContact.visibility = View.VISIBLE
        }
    }

    private fun displayFoundItemData(item: FoundItemResponse) {
        binding.apply {
            // Set title
            tvTitle.text = "Found Item Details"

            // Item information
            tvItemName.text = item.title
            tvCategory.text = item.categoryName
            tvDateValue.text = formatDate(item.foundDate)
            tvTimeValue.text = item.foundTime
            tvLocationValue.text = item.foundLocation
            tvDescription.text = item.description

            // Storage location
            tvStorageLocationLabel.visibility = View.VISIBLE
            tvStorageLocationValue.visibility = View.VISIBLE
            tvStorageLocationValue.text = item.storageLocation

            // Additional details
            if (item.brand.isNotEmpty()) {
                tvBrandLabel.visibility = View.VISIBLE
                tvBrandValue.visibility = View.VISIBLE
                tvBrandValue.text = item.brand
            }

            if (item.color.isNotEmpty()) {
                tvColorLabel.visibility = View.VISIBLE
                tvColorValue.visibility = View.VISIBLE
                tvColorValue.text = item.color
            }

            if (item.size.isNotEmpty()) {
                tvSizeLabel.visibility = View.VISIBLE
                tvSizeValue.visibility = View.VISIBLE
                tvSizeValue.text = item.size
            }

            // Status badge
            tvStatus.text = item.status.uppercase()
            updateStatusBadge(item.status)

            // Tags
            if (item.searchTagsList.isNotEmpty()) {
                tvTagsLabel.visibility = View.VISIBLE
                tvTagsValue.visibility = View.VISIBLE
                tvTagsValue.text = item.searchTagsList.joinToString(", ")
            }

            // Load image
            val imageUrl = item.imageUrl ?: item.itemImage
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(ivItemImage)
            }

            // Button visibility for found items
            btnClaimItem.visibility = if (item.status == "found") View.VISIBLE else View.GONE
            btnViewClaims.visibility = View.GONE
            btnContact.visibility = View.VISIBLE
        }
    }

    private fun updateStatusBadge(status: String) {
        val statusColor = when (status.lowercase()) {
            "lost" -> R.color.orange
            "found" -> R.color.primary_teal
            "claimed" -> R.color.blue
            "returned" -> R.color.green
            else -> R.color.dark_gray
        }
        binding.tvStatus.setBackgroundResource(R.drawable.rounded_button_black)
        binding.tvStatus.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), statusColor)
    }

    private fun showClaimDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Claim This Item")
            .setMessage("Do you want to claim this found item? You'll need to provide proof of ownership.")
            .setPositiveButton("Claim") { _, _ ->
                navigateToClaimForm()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun navigateToClaimForm() {
        currentFoundItem?.let { item ->
            val bundle = Bundle().apply {
                putString("foundItemId", item.id)
                putString("foundItemTitle", item.title)
                putString("foundItemImage", item.imageUrl ?: item.itemImage)
            }
            navigateTo(R.id.action_itemDetailFragment_to_claimFragment, bundle)

        }
    }

    private fun viewClaims() {
        Toast.makeText(requireContext(), "View claims coming soon", Toast.LENGTH_SHORT).show()
    }

    /**
     * Contact item owner - Opens SendMessageDialogFragment
     * This allows users to send a message to the item owner
     */
    private fun contactItemOwner() {
        // Get item details for the message
        val itemTitle = when (itemType) {
            ItemType.LOST -> currentLostItem?.title
            ItemType.FOUND -> currentFoundItem?.title
        } ?: "Item"

        val itemId = when (itemType) {
            ItemType.LOST -> currentLostItem?.id
            ItemType.FOUND -> currentFoundItem?.id
        }

        val itemTypeStr = when (itemType) {
            ItemType.LOST -> "item_lost"
            ItemType.FOUND -> "item_found"
        }

        // Open send message dialog
        SendMessageDialogFragment.newInstance(
            recipientTitle = "Owner of $itemTypeStr: $itemTitle",
            relatedItemId = itemId,
            type = itemTypeStr
        ).show(childFragmentManager, "SendMessageDialog")
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}