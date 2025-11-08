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
import com.example.lostandfound.feature.base.ApiErrorDialog
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.chat.SendMessageDialogFragment
import com.example.lostandfound.feature.claimitem.ClaimViewModel
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
    private var currentItem: LostItemResponse? = null
    private var currentFoundItem: FoundItemResponse? = null

    enum class ItemType {
        LOST, FOUND
    }

    private val ARG_ITEM_ID = "itemId"
    private val ARG_ITEM_TYPE = "itemType"


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

        binding.btnEdit.setOnClickListener {
            if (itemType == ItemType.LOST) {
                navigateTo(R.id.action_addItemFragment_to_reportLostItemFragment, Bundle().apply {
                    putBoolean("isEditMode", true)
                    putSerializable("item", currentItem)
                })
            } else {
                navigateTo(
                    R.id.action_itemDetailFragment_to_reportFoundItemFragment,
                    Bundle().apply {
                        putBoolean("isEditMode", true)
                        putSerializable("item", currentFoundItem)
                    })
            }
        }

        binding.btnDelete.setOnClickListener {
            ApiErrorDialog.showCustom(
                context = requireContext(),
                title = "Info",
                isCancelable = false,
                message = "Are you sure you want to delete this item?",
                showRetry = true,
                onAction = {},
                onOkAction = {
                    if (itemType == ItemType.LOST) {
                        itemViewModel.deleteLostItem(itemId ?: "")
                    } else {
                        itemViewModel.deleteFoundItem(itemId ?: "")
                    }
                }
            )
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
        }
    }

    private fun observeViewModels() {
        // Observe Item Detail (works for both Lost and Found)
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.itemDetailState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Loading item details...")
                    is Resource.Success -> {
                        hideLoading()
                        when (itemType) {
                            ItemType.LOST -> {
                                currentItem = resource.data
                                displayItemData(item = resource.data)
                            }
                            ItemType.FOUND -> {
                                // For found items, store in currentFoundItem but display using same method
                                currentItem = resource.data
                                // Convert to FoundItemResponse if needed for navigation
                                currentFoundItem = FoundItemResponse(
                                    id = resource.data.id,
                                    user = resource.data.user,
                                    title = resource.data.title,
                                    description = resource.data.description,
                                    category = resource.data.category,
                                    categoryName = resource.data.categoryName,
                                    searchTags = resource.data.searchTags,
                                    colorTags = resource.data.colorTags,
                                    materialTags = resource.data.materialTags,
                                    foundLocation = resource.data.foundLocation ?: "",
                                    foundDate = resource.data.foundDate ?: "",
                                    foundTime = resource.data.foundTime ?: "",
                                    brand = resource.data.brand,
                                    color = resource.data.color,
                                    size = resource.data.size,
                                    itemImage = resource.data.itemImage,
                                    imageUrl = resource.data.imageUrl,
                                    status = resource.data.status,
                                    isVerified = resource.data.isVerified,
                                    createdAt = resource.data.createdAt,
                                    updatedAt = resource.data.updatedAt,
                                    searchTagsList = resource.data.searchTagsList,
                                    colorTagsList = resource.data.colorTagsList,
                                    materialTagsList = resource.data.materialTagsList,
                                    storageLocation = resource.data.storageLocation ?: ""
                                )
                                displayItemData(item = resource.data)
                            }
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load item: ${resource.exception.message}")
                    }
                    Resource.None -> hideLoading()
                }
            }
        }

        // Observe Delete Lost Item
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.rejectLostItemState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Deleting item...")
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Item deleted successfully")
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to delete item: ${resource.exception.message}")
                    }
                    Resource.None -> hideLoading()
                }
            }
        }

        // Observe Delete Found Item
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.rejectFoundItemState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Deleting item...")
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Item deleted successfully")
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to delete item: ${resource.exception.message}")
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

    /**
     * Unified method to display item data for both Lost and Found items
     * Handles common fields and type-specific differences
     */
    private fun displayItemData(item: LostItemResponse) {
        binding.apply {
            // Determine if this is a found item based on the presence of found-specific fields
            val isFoundItem = !item.foundDate.isNullOrEmpty() || !item.foundLocation.isNullOrEmpty()

            // Determine if current user owns this item
            val isOwnItem = item.user.contains(AuthData.userDetailInfo?.username ?: "")

            // Common item information
            tvItemName.text = item.title
            tvCategory.text = item.categoryName
            tvDescription.text = item.description

            // Date, Time, and Location (different field names for lost vs found)
            if (isFoundItem) {
                tvDateValue.text = formatDate(item.foundDate ?: "")
                tvTimeValue.text = item.foundTime ?: ""
                tvLocationValue.text = item.foundLocation ?: ""
            } else {
                tvDateValue.text = formatDate(item.lostDate ?: "")
                tvTimeValue.text = item.lostTime ?: ""
                tvLocationValue.text = item.lostLocation ?: ""
            }

            // Storage location (only for found items)
            if (isFoundItem && !item.storageLocation.isNullOrEmpty()) {
                tvStorageLocationLabel.visibility = View.VISIBLE
                tvStorageLocationValue.visibility = View.VISIBLE
                tvStorageLocationValue.text = item.storageLocation
            } else {
                tvStorageLocationLabel.visibility = View.GONE
                tvStorageLocationValue.visibility = View.GONE
            }

            // Optional details: Brand
            if (!item.brand.isNullOrEmpty()) {
                tvBrandLabel.visibility = View.VISIBLE
                tvBrandValue.visibility = View.VISIBLE
                tvBrandValue.text = item.brand
            } else {
                tvBrandLabel.visibility = View.GONE
                tvBrandValue.visibility = View.GONE
            }

            // Optional details: Color
            if (!item.color.isNullOrEmpty()) {
                tvColorLabel.visibility = View.VISIBLE
                tvColorValue.visibility = View.VISIBLE
                tvColorValue.text = item.color
            } else {
                tvColorLabel.visibility = View.GONE
                tvColorValue.visibility = View.GONE
            }

            // Optional details: Size
            if (!item.size.isNullOrEmpty()) {
                tvSizeLabel.visibility = View.VISIBLE
                tvSizeValue.visibility = View.VISIBLE
                tvSizeValue.text = item.size
            } else {
                tvSizeLabel.visibility = View.GONE
                tvSizeValue.visibility = View.GONE
            }

            // Status badge
            tvStatus.text = item.status.uppercase()
            updateStatusBadge(item.status)

            // Tags
            if (item.searchTagsList.isNotEmpty()) {
                tvTagsLabel.visibility = View.VISIBLE
                tvTagsValue.visibility = View.VISIBLE
                tvTagsValue.text = item.searchTagsList.joinToString(", ")
            } else {
                tvTagsLabel.visibility = View.GONE
                tvTagsValue.visibility = View.GONE
            }

            // Load item image
            val imageUrl = item.imageUrl ?: item.itemImage

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(ivItemImage)
            }

            // Button visibility logic
            if (isFoundItem) {
                // Found item buttons
                if (isOwnItem) {
                    // Owner of found item
                    layoutMypost.visibility = View.VISIBLE
                    btnClaimItem.visibility = View.GONE
                } else {
                    // Not the owner - can claim if status is "found"
                    layoutMypost.visibility = View.GONE
                    btnClaimItem.visibility = if (item.status.equals("found", ignoreCase = true)) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }

                btnViewClaims.visibility = View.GONE
                btnContact.visibility = if (!isOwnItem) View.VISIBLE else View.GONE

            } else {
                // Lost item buttons
                if (isOwnItem) {
                    // Owner of lost item
                    btnClaimItem.visibility = View.GONE
                    layoutMypost.visibility = View.VISIBLE
                } else {
                    // Not the owner - show contact button
                    layoutMypost.visibility = View.GONE
                    btnClaimItem.visibility = View.GONE
                }

                btnViewClaims.visibility = if (AuthData.userDetailInfo?.userType == "admin") {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                btnContact.visibility = if (!isOwnItem) View.VISIBLE else View.GONE
            }
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
        } ?: run {
            // Fallback if currentFoundItem is null
            currentItem?.let { item ->
                val bundle = Bundle().apply {
                    putString("foundItemId", item.id)
                    putString("foundItemTitle", item.title)
                    putString("foundItemImage", item.imageUrl ?: item.itemImage)
                }
                navigateTo(R.id.action_itemDetailFragment_to_claimFragment, bundle)
            }
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
        val itemTitle = currentItem?.title ?: "Item"
        val itemId = currentItem?.id

        val itemTypeStr = when (itemType) {
            ItemType.LOST -> "lost_item"
            ItemType.FOUND -> "found_item"
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