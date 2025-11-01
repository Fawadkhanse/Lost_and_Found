package com.example.lostandfound.feature.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ItemPostRequestBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying post requests that need admin verification
 */
class PostRequestsAdapter(
    private val onApprove: (String) -> Unit,
    private val onReject: (String) -> Unit,
    private val onView: (String) -> Unit
) : ListAdapter<PostRequestItem, PostRequestsAdapter.PostRequestViewHolder>(PostRequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostRequestViewHolder {
        val binding = ItemPostRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostRequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostRequestViewHolder(
        private val binding: ItemPostRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostRequestItem) {
            binding.apply {
                // Set item details
                tvItemTitle.text = item.title
                tvCategory.text = item.category
                tvDate.text = formatDate(item.date)
                tvLocation.text = item.location
                tvDescription.text = item.description
                tvUser.text = "Posted by: ${item.user}"

                // Load image
                if (!item.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(item.imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(ivItemImage)
                } else {
                    ivItemImage.setImageResource(R.drawable.ic_placeholder)
                }

                // Set button listeners
                btnApprove.setOnClickListener {
                    onApprove(item.id)
                }

                btnReject.setOnClickListener {
                    onReject(item.id)
                }

                root.setOnClickListener {
                    onView(item.id)
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }
    }

    class PostRequestDiffCallback : DiffUtil.ItemCallback<PostRequestItem>() {
        override fun areItemsTheSame(oldItem: PostRequestItem, newItem: PostRequestItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostRequestItem, newItem: PostRequestItem): Boolean {
            return oldItem == newItem
        }
    }
}