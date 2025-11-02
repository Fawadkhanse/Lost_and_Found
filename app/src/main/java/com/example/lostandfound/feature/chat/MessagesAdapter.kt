package com.example.lostandfound.feature.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ItemMessageBinding
import com.example.lostandfound.domain.auth.NotificationResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying messages/notifications in a list
 */
class MessagesAdapter(
    private val onItemClick: (NotificationResponse) -> Unit,
    private val onDeleteClick: (NotificationResponse) -> Unit
) : ListAdapter<NotificationResponse, MessagesAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationResponse) {
            binding.apply {
                // Set title
                tvMessageTitle.text = notification.title

                // Set message preview
                tvMessagePreview.text = notification.message

                // Set timestamp
                tvTimestamp.text = formatTimestamp(notification.createdAt)

                // Set type badge
                when (notification.notificationType) {
                    "item_found" -> {
                        tvTypeBadge.text = "Item Found"
                        tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                        tvTypeBadge.backgroundTintList =
                            itemView.context.getColorStateList(R.color.primary_teal)
                        ivTypeIcon.setImageResource(R.drawable.ic_found)
                    }
                    "match_found" -> {
                        tvTypeBadge.text = "Match"
                        tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                        tvTypeBadge.backgroundTintList =
                            itemView.context.getColorStateList(R.color.green)
                        ivTypeIcon.setImageResource(R.drawable.ic_check)
                    }
                    "claim_update" -> {
                        tvTypeBadge.text = "Claim"
                        tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                        tvTypeBadge.backgroundTintList =
                            itemView.context.getColorStateList(R.color.blue)
                        ivTypeIcon.setImageResource(R.drawable.ic_claim)
                    }
                    "system" -> {
                        tvTypeBadge.text = "System"
                        tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_gray)
                        ivTypeIcon.setImageResource(R.drawable.ic_notif)
                    }
                    "message" -> {
                        tvTypeBadge.text = "Message"
                        tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_black)
                        tvTypeBadge.backgroundTintList =
                            itemView.context.getColorStateList(R.color.primary_teal)
                        ivTypeIcon.setImageResource(R.drawable.ic_message)
                    }
                    else -> {
                        tvTypeBadge.text = "Notification"
                        tvTypeBadge.setBackgroundResource(R.drawable.rounded_button_gray)
                        ivTypeIcon.setImageResource(R.drawable.ic_notif)
                    }
                }

                // Show/hide unread indicator
                if (notification.isRead) {
                    viewUnreadIndicator.visibility = View.GONE
                    // Make text normal weight
                    tvMessageTitle.setTypeface(null, android.graphics.Typeface.NORMAL)
                } else {
                    viewUnreadIndicator.visibility = View.VISIBLE
                    // Make text bold
                    tvMessageTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                }

                // Click listeners
                root.setOnClickListener {
                    onItemClick(notification)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(notification)
                }

                // Long press for options
                root.setOnLongClickListener {
                    onDeleteClick(notification)
                    true
                }
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(timestamp)

                val now = Calendar.getInstance()
                val messageTime = Calendar.getInstance()
                messageTime.time = date

                when {
                    // Today
                    now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                            now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) -> {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        timeFormat.format(date)
                    }
                    // Yesterday
                    now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                            now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1 -> {
                        "Yesterday"
                    }
                    // This year
                    now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        dateFormat.format(date)
                    }
                    // Other years
                    else -> {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        dateFormat.format(date)
                    }
                }
            } catch (e: Exception) {
                timestamp
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<NotificationResponse>() {
        override fun areItemsTheSame(
            oldItem: NotificationResponse,
            newItem: NotificationResponse
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: NotificationResponse,
            newItem: NotificationResponse
        ): Boolean {
            return oldItem == newItem
        }
    }
}