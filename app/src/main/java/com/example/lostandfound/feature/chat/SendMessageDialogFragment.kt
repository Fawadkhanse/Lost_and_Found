package com.example.lostandfound.feature.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.DialogSendMessageBinding
import com.example.lostandfound.feature.notification.NotificationViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Dialog for composing and sending messages
 * Can be used to send messages to item owners or admins
 */
class SendMessageDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogSendMessageBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by viewModel()

    private var recipientTitle: String? = null
    private var relatedItemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipientTitle = it.getString(ARG_RECIPIENT_TITLE)
            relatedItemId = it.getString(ARG_RELATED_ITEM_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSendMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        // Set recipient if provided
        recipientTitle?.let {
            binding.tvRecipient.text = it
        } ?: run {
            binding.tvRecipient.text = "General Message"
        }
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            notificationViewModel.createNotificationState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Message sent successfully!")
                        notificationViewModel.resetCreateState()
                        dismiss()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to send message: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun sendMessage() {
        val title = binding.etMessageTitle.text.toString().trim()
        val content = binding.etMessageContent.text.toString().trim()

        // Validation
        if (title.isEmpty()) {
            binding.tilMessageTitle.error = "Subject is required"
            return
        }

        if (content.isEmpty()) {
            binding.tilMessageContent.error = "Message is required"
            return
        }

        // Clear errors
        binding.tilMessageTitle.error = null
        binding.tilMessageContent.error = null

        // Send message
        notificationViewModel.sendMessageToOwner(
            recipientTitle = recipientTitle ?: "User",
            messageTitle = title,
            messageContent = content,
            relatedItemId = relatedItemId
        )
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false
        binding.btnCancel.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSend.isEnabled = true
        binding.btnCancel.isEnabled = true
    }

    private fun showSuccess(message: String) {
        android.widget.Toast.makeText(
            requireContext(),
            message,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(
            requireContext(),
            message,
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_RECIPIENT_TITLE = "recipient_title"
        private const val ARG_RELATED_ITEM_ID = "related_item_id"

        fun newInstance(
            recipientTitle: String? = null,
            relatedItemId: String? = null
        ): SendMessageDialogFragment {
            return SendMessageDialogFragment().apply {
                arguments = Bundle().apply {
                    recipientTitle?.let { putString(ARG_RECIPIENT_TITLE, it) }
                    relatedItemId?.let { putString(ARG_RELATED_ITEM_ID, it) }
                }
            }
        }
    }
}