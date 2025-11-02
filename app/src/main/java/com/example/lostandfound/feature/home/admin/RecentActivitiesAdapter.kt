package com.example.lostandfound.feature.home.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ItemRecentActivityBinding
import com.example.lostandfound.domain.auth.ActivityItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Adapter for displaying recent activities in admin dashboard
 */
class RecentActivitiesAdapter(
    private val onItemClick: (ActivityItem) -> Unit
) : ListAdapter<ActivityItem, RecentActivitiesAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemRecentActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivityViewHolder(
        private val binding: ItemRecentActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: ActivityItem) {
            binding.apply {
                // Set activity title
                tvActivityTitle.text = activity.title

                // Set activity date
                tvActivityDate.text = formatDate(activity.date)

                // Set activity icon and color based on type
                when (activity.type) {
                    "claim" -> {
                        ivActivityIcon.setImageResource(R.drawable.ic_claim)
                        ivActivityIcon.setColorFilter(itemView.context.getColor(R.color.blue))

                        // Show status if available
                        activity.status?.let {
                            tvActivityStatus.visibility = View.VISIBLE
                            tvActivityStatus.text = it.uppercase()
                            tvActivityStatus.setBackgroundResource(
                                when (it.lowercase()) {
                                    "pending" -> R.drawable.rounded_button_black
                                    "approved" -> R.drawable.rounded_button_black
                                    "rejected" -> R.drawable.rounded_button_red
                                    else -> R.drawable.rounded_button_gray
                                }
                            )
                            tvActivityStatus.backgroundTintList = itemView.context.getColorStateList(
                                when (it.lowercase()) {
                                    "pending" -> R.color.orange
                                    "approved" -> R.color.green
                                    "rejected" -> R.color.red
                                    else -> R.color.dark_gray
                                }
                            )
                        } ?: run {
                            tvActivityStatus.visibility = View.GONE
                        }
                    }
                    "found_item" -> {
                        ivActivityIcon.setImageResource(R.drawable.ic_found)
                        ivActivityIcon.setColorFilter(itemView.context.getColor(R.color.primary_teal))

                        activity.status?.let {
                            tvActivityStatus.visibility = View.VISIBLE
                            tvActivityStatus.text = it.uppercase()
                            tvActivityStatus.setBackgroundResource(R.drawable.rounded_button_black)
                            tvActivityStatus.backgroundTintList =
                                itemView.context.getColorStateList(R.color.primary_teal)
                        } ?: run {
                            tvActivityStatus.visibility = View.GONE
                        }
                    }
                    "lost_item" -> {
                        ivActivityIcon.setImageResource(R.drawable.ic_lost)
                        ivActivityIcon.setColorFilter(itemView.context.getColor(R.color.orange))

                        activity.status?.let {
                            tvActivityStatus.visibility = View.VISIBLE
                            tvActivityStatus.text = it.uppercase()
                            tvActivityStatus.setBackgroundResource(R.drawable.rounded_button_gray)
                        } ?: run {
                            tvActivityStatus.visibility = View.GONE
                        }
                    }
                    "user_registration" -> {
                        ivActivityIcon.setImageResource(R.drawable.ic_account)
                        ivActivityIcon.setColorFilter(itemView.context.getColor(R.color.green))

                        activity.userType?.let {
                            tvActivityStatus.visibility = View.VISIBLE
                            tvActivityStatus.text = it.uppercase()
                            tvActivityStatus.setBackgroundResource(R.drawable.rounded_button_black)
                            tvActivityStatus.backgroundTintList =
                                itemView.context.getColorStateList(
                                    if (it == "admin") R.color.blue else R.color.green
                                )
                        } ?: run {
                            tvActivityStatus.visibility = View.GONE
                        }
                    }
                    else -> {
                        ivActivityIcon.setImageResource(R.drawable.ic_logo)
                        ivActivityIcon.setColorFilter(itemView.context.getColor(R.color.dark_gray))
                        tvActivityStatus.visibility = View.GONE
                    }
                }

                // Set user info if available
                activity.user?.let {
                    tvActivityUser.visibility = View.VISIBLE
                    tvActivityUser.text = "by $it"
                } ?: run {
                    tvActivityUser.visibility = View.GONE
                }

                // Set click listener
                root.setOnClickListener {
                    onItemClick(activity)
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateString)

                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                try {
                    // Fallback format
                    val inputFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    val date = inputFormat.parse(dateString)
                    val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    date?.let { outputFormat.format(it) } ?: dateString
                } catch (e: Exception) {
                    dateString
                }
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityItem>() {
        override fun areItemsTheSame(oldItem: ActivityItem, newItem: ActivityItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityItem, newItem: ActivityItem): Boolean {
            return oldItem == newItem
        }
    }
}