package com.example.lostandfound.feature.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentChatBinding
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.item.ItemViewModel
import com.example.lostandfound.feature.notification.NotificationViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

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
    }

    private fun setupViews() {
        // Set title
        binding.tvChatTitle.text = title ?: "Message"
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // View related item button
        binding.btnViewItem.setOnClickListener {
            viewRelatedItem()
        }

        // View related claim button
        binding.btnViewClaim.setOnClickListener {
            viewRelatedClaim()
        }

        // Reply button (if applicable)
        binding.btnReply.setOnClickListener {
            replyToMessage()
        }
    }

    private fun displayMessageContent() {
        binding.apply {
            // Display message type badge
            when (type) {
                "message" -> {
                    tvTypeBadge.text = "Message"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                    tvTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.primary_teal)
                }
                "claim_update" -> {
                    tvTypeBadge.text = "Claim Update"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                    tvTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.blue)
                }
                "item_match" -> {
                    tvTypeBadge.text = "Item Match"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                    tvTypeBadge.backgroundTintList =
                        requireContext().getColorStateList(R.color.green)
                }
                "system" -> {
                    tvTypeBadge.text = "System"
                    tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_gray)
                    // Hide reply button for system messages
                    btnReply.visibility = View.GONE
                }
            }

            // Display message content
            tvMessageTitle.text = title
            tvMessageContent.text = message
            tvTimestamp.text = "Just now" // You can pass actual timestamp

            // Show/hide action buttons based on related items
            if (lostItemId != null || foundItemId != null) {
                layoutRelatedItem.visibility = View.VISIBLE
                btnViewItem.visibility = View.VISIBLE

                // Load item details preview
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

            // Hide reply for now (can be implemented with a reply dialog)
            if (type == "message") {
                btnReply.visibility = View.VISIBLE
            } else {
                btnReply.visibility = View.GONE
            }
        }
    }

    private fun loadItemPreview() {
        binding.apply {
            // Show preview card
            cardItemPreview.visibility = View.VISIBLE

            // Set placeholder while loading
            tvItemPreviewTitle.text = "Loading item details..."
            tvItemPreviewDescription.text = ""

            // Load actual item details
            when {
                lostItemId != null -> {
                    // Load lost item
                    tvItemPreviewLabel.text = "Related Lost Item"
                    // You can load item details here using itemViewModel
                    // For now, just show placeholder
                }
                foundItemId != null -> {
                    // Load found item
                    tvItemPreviewLabel.text = "Related Found Item"
                    // You can load item details here using itemViewModel
                    // For now, just show placeholder
                }
            }
        }
    }

    private fun viewRelatedItem() {
        val itemId = lostItemId ?: foundItemId
        if (itemId != null) {
            val bundle = Bundle().apply {
                putString("itemId", itemId)
                putString("itemType", if (lostItemId != null) "LOST" else "FOUND")
            }

            // Navigate to item detail
            Toast.makeText(
                requireContext(),
                "Viewing item: $itemId",
                Toast.LENGTH_SHORT
            ).show()

            // TODO: Navigate to item detail fragment
            // findNavController().navigate(
            //     R.id.action_chatFragment_to_itemDetailFragment,
            //     bundle
            // )
        }
    }

    private fun viewRelatedClaim() {
        if (claimId != null) {
            Toast.makeText(
                requireContext(),
                "Viewing claim: $claimId",
                Toast.LENGTH_SHORT
            ).show()

            // TODO: Navigate to claim detail fragment
            // val bundle = Bundle().apply {
            //     putString("claimId", claimId)
            // }
            // findNavController().navigate(
            //     R.id.action_chatFragment_to_claimDetailFragment,
            //     bundle
            // )
        }
    }

    private fun replyToMessage() {
        // Show reply dialog or navigate to send message screen
        SendMessageDialogFragment.newInstance(
            recipientTitle = title ?: "Reply",
            relatedItemId = lostItemId ?: foundItemId
        ).show(childFragmentManager, "SendMessageDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            messageId: String,
            title: String,
            message: String,
            type: String
        ): ChatFragment {
            return ChatFragment().apply {
                arguments = Bundle().apply {
                    putString("messageId", messageId)
                    putString("title", title)
                    putString("message", message)
                    putString("type", type)
                }
            }
        }
    }
}