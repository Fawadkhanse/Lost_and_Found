package com.example.lostandfound.feature.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentMessagesBinding
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.notification.NotificationViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * MessagesFragment - FIXED VERSION
 * Displays list of all messages/notifications
 * Shows notifications grouped and sorted by date
 */
class MessagesFragment : BaseFragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by viewModel()
    private lateinit var messagesAdapter: MessagesAdapter

    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh messages when returning to this fragment
        loadMessages()
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(
            onItemClick = { notification ->
                navigateToChat(notification)
            },
            onDeleteClick = { notification ->
                deleteMessage(notification.id)
            }
        )

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messagesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadMessages()
        }

        // Filter buttons
        binding.btnAll.setOnClickListener {
            filterMessages("all")
        }

        binding.btnUnread.setOnClickListener {
            filterMessages("unread")
        }

        binding.btnSystem.setOnClickListener {
            filterMessages("system")
        }

        binding.btnMessages.setOnClickListener {
            filterMessages("message")
        }

        // Compose new message
        binding.fabCompose.setOnClickListener {
            SendMessageDialogFragment.newInstance()
                .show(childFragmentManager, "SendMessageDialog")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            notificationViewModel.notificationsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.swipeRefresh.isRefreshing = true
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.swipeRefresh.isRefreshing = false
                        binding.progressBar.visibility = View.GONE

                        val notifications = resource.data.results

                        // Apply current filter
                        val filteredNotifications = when (currentFilter) {
                            "unread" -> notifications.filter { !it.isRead }
                            "system" -> notifications.filter { it.notificationType == "system" }
                            "message" -> notifications.filter { it.notificationType == "message" }
                            else -> notifications
                        }

                        // Submit to adapter with new list to trigger DiffUtil
                        messagesAdapter.submitList(filteredNotifications.toList())

                        // Update empty state
                        if (filteredNotifications.isEmpty()) {
                            binding.tvNoMessages.visibility = View.VISIBLE
                            binding.rvMessages.visibility = View.GONE
                        } else {
                            binding.tvNoMessages.visibility = View.GONE
                            binding.rvMessages.visibility = View.VISIBLE
                        }

                        // Update unread badge
                        val unreadCount = notifications.count { !it.isRead }
                        updateUnreadBadge(unreadCount)
                    }
                    is Resource.Error -> {
                        binding.swipeRefresh.isRefreshing = false
                        binding.progressBar.visibility = View.GONE
                        showError("Failed to load messages: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        binding.swipeRefresh.isRefreshing = false
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }

        // Observe unread count
        viewLifecycleOwner.lifecycleScope.launch {
            notificationViewModel.unreadCount.collect { count ->
                updateUnreadBadge(count)
            }
        }
    }

    private fun loadMessages() {
        notificationViewModel.getAllNotifications()
    }

    private fun filterMessages(filter: String) {
        currentFilter = filter

        // Update button states
        resetFilterButtons()

        when (filter) {
            "all" -> {
                binding.btnAll.setBackgroundResource(R.drawable.rounded_button_black)
                binding.btnAll.backgroundTintList =
                    requireContext().getColorStateList(R.color.primary_teal)
            }
            "unread" -> {
                binding.btnUnread.setBackgroundResource(R.drawable.rounded_button_black)
                binding.btnUnread.backgroundTintList =
                    requireContext().getColorStateList(R.color.primary_teal)
            }
            "system" -> {
                binding.btnSystem.setBackgroundResource(R.drawable.rounded_button_black)
                binding.btnSystem.backgroundTintList =
                    requireContext().getColorStateList(R.color.primary_teal)
            }
            "message" -> {
                binding.btnMessages.setBackgroundResource(R.drawable.rounded_button_black)
                binding.btnMessages.backgroundTintList =
                    requireContext().getColorStateList(R.color.primary_teal)
            }
        }

        // Reload to apply filter
        val currentState = notificationViewModel.notificationsListState.value
        if (currentState is Resource.Success) {
            val notifications = currentState.data.results

            val filteredNotifications = when (filter) {
                "unread" -> notifications.filter { !it.isRead }
                "system" -> notifications.filter { it.notificationType == "system" }
                "message" -> notifications.filter { it.notificationType == "message" }
                else -> notifications
            }

            messagesAdapter.submitList(filteredNotifications.toList())

            // Update empty state
            if (filteredNotifications.isEmpty()) {
                binding.tvNoMessages.visibility = View.VISIBLE
                binding.rvMessages.visibility = View.GONE
            } else {
                binding.tvNoMessages.visibility = View.GONE
                binding.rvMessages.visibility = View.VISIBLE
            }
        }
    }

    private fun resetFilterButtons() {
        binding.btnAll.setBackgroundResource(R.drawable.rounded_button_gray)
        binding.btnAll.backgroundTintList = null

        binding.btnUnread.setBackgroundResource(R.drawable.rounded_button_gray)
        binding.btnUnread.backgroundTintList = null

        binding.btnSystem.setBackgroundResource(R.drawable.rounded_button_gray)
        binding.btnSystem.backgroundTintList = null

        binding.btnMessages.setBackgroundResource(R.drawable.rounded_button_gray)
        binding.btnMessages.backgroundTintList = null
    }

    private fun updateUnreadBadge(count: Int) {
        if (count > 0) {
            binding.tvUnreadBadge.visibility = View.VISIBLE
            binding.tvUnreadBadge.text = if (count > 99) "99+" else count.toString()
        } else {
            binding.tvUnreadBadge.visibility = View.GONE
        }
    }

    private fun navigateToChat(notification: com.example.lostandfound.domain.auth.NotificationResponse) {
        // Mark as read before navigating
        notificationViewModel.markNotificationAsRead(notification.id)

        val bundle = Bundle().apply {
            putString("messageId", notification.id)
            putString("title", notification.title)
            putString("message", notification.message)
            putString("type", notification.notificationType)
            putString("lostItemId", notification.lostItem)
            putString("foundItemId", notification.foundItem)
            putString("claimId", notification.claim)
        }

        navigateTo(
            R.id.action_messagesFragment_to_chatFragment,
            bundle
        )
    }

    private fun deleteMessage(messageId: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                notificationViewModel.deleteNotification(messageId)
                showSuccess("Message deleted")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}