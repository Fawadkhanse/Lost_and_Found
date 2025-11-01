package com.example.lostandfound.feature.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentChatBinding
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.item.ItemViewModel
import com.example.lostandfound.feature.notification.NotificationViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * ChatFragment - Individual chat/message detail view
 * Displays full message content and related items/claims
 */
class ChatFragment : BaseFragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by viewModel()
    private val itemViewModel: ItemViewModel by viewModel()

    private var messageId: String? = null
    private var title: String? = null
    private var message: String? = null
    private var type: String? = null
    private var lostItemId: String? = null
    private var foundItemId: String? = null
    private var claimId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            messageId = it.getString("messageId")
            title = it.getString("title")
            message = it.getString("message")
            type = it.getString("type")
            lostItemId = it.getString("lostItemId")
            foundItemId = it.getString("foundItemId")
            claimId = it.getString("claimId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        displayMessageContent()
        markMessageAsRead()
    }

    private fun setupViews() {
        binding.tvChatTitle.text = title ?: "Message"
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnViewItem.setOnClickListener {
            viewRelatedItem()
        }

        binding.btnViewClaim.setOnClickListener {
            viewRelatedClaim()
        }

        binding.btnReply.setOnClickListener {
            replyToMessage()
        }
    }

    private fun displayMessageContent() {
        binding.apply {
            // Display message type badge
            when (type) {
                "item_found" -> {
                    tvTypeBadge.text = "Item Found"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                    tvTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.primary_teal)
                }
                "match_found" -> {
                    tvTypeBadge.text = "Match Found"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                    tvTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.green)
                }
                "claim_update" -> {
                    tvTypeBadge.text = "Claim Update"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                    tvTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.blue)
                }
                "system" -> {
                    tvTypeBadge.text = "System"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_gray)
                    btnReply.visibility = View.GONE
                }
                "message" -> {
                    tvTypeBadge.text = "Message"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                    tvTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.primary_teal)
                }
            }

            // Display message content
            tvMessageTitle.text = title
            tvMessageContent.text = message
            tvTimestamp.text = "Just now"

            // Show/hide action buttons based on related items
            if (lostItemId != null || foundItemId != null) {
                layoutRelatedItem.visibility = View.VISIBLE
                btnViewItem.visibility = View.VISIBLE
                loadItemPreview()
            } else {
                layoutRelatedItem.visibility = View.GONE
                btnViewItem.visibility = View.GONE
            }

            if (claimId != null) {
                btnViewClaim.visibility = View.VISIBLE
            } else {
                btnViewClaim.visibility = View.GONE
            }

            // Show reply button for messages
            if (type == "message") {
                btnReply.visibility = View.VISIBLE
            } else {
                btnReply.visibility = View.GONE
            }
        }
    }

    private fun loadItemPreview() {
        binding.apply {
            cardItemPreview.visibility = View.VISIBLE
            tvItemPreviewTitle.text = "Loading item details..."
            tvItemPreviewDescription.text = ""

            when {
                lostItemId != null -> {
                    tvItemPreviewLabel.text = "Related Lost Item"
                    itemViewModel.getLostItemById(lostItemId!!)
                    observeLostItem()
                }
                foundItemId != null -> {
                    tvItemPreviewLabel.text = "Related Found Item"
                    itemViewModel.getFoundItemById(foundItemId!!)
                    observeFoundItem()
                }
            }
        }
    }

    private fun observeLostItem() {
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.lostItemDetailState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.tvItemPreviewTitle.text = resource.data.title
                        binding.tvItemPreviewDescription.text =
                            "Lost at ${resource.data.lostLocation}"
                    }
                    is Resource.Error -> {
                        binding.tvItemPreviewTitle.text = "Failed to load item"
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeFoundItem() {
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.foundItemDetailState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.tvItemPreviewTitle.text = resource.data.title
                        binding.tvItemPreviewDescription.text =
                            "Found at ${resource.data.foundLocation}"
                    }
                    is Resource.Error -> {
                        binding.tvItemPreviewTitle.text = "Failed to load item"
                    }
                    else -> {}
                }
            }
        }
    }

    private fun markMessageAsRead() {
        messageId?.let {
            notificationViewModel.markNotificationAsRead(it)
        }
    }

    private fun viewRelatedItem() {
        val itemId = lostItemId ?: foundItemId
        val itemType = if (lostItemId != null) "LOST" else "FOUND"

        if (itemId != null) {
            val bundle = Bundle().apply {
                putString("itemId", itemId)
                putString("itemType", itemType)
            }

            try {
                findNavController().navigate(
                    R.id.action_chatFragment_to_itemDetailFragment,
                    bundle
                )
            } catch (e: Exception) {
                showError("Navigation error: ${e.message}")
            }
        }
    }

    private fun viewRelatedClaim() {
        if (claimId != null) {
            showInfo("Claim details coming soon")
            // TODO: Navigate to claim detail when available
        }
    }

    private fun replyToMessage() {
        SendMessageDialogFragment.newInstance(
            recipientTitle = "Reply to: $title",
            relatedItemId = lostItemId ?: foundItemId
        ).show(childFragmentManager, "SendMessageDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}