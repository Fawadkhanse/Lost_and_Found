package com.example.lostandfound.feature.mylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ItemMyListBinding

/**
 * Adapter for My List items (Claims and Posts)
 */
class MyListAdapter(
    private val onItemClick: (MyListItem) -> Unit
) : ListAdapter<MyListItem, MyListAdapter.MyListViewHolder>(MyListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyListViewHolder {
        val binding = ItemMyListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyListViewHolder(
        private val binding: ItemMyListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyListItem) {
            binding.apply {
                // Set item name
                tvItemName.text = item.title

                // Set status button
                btnStatus.text = item.status

                // Set status button color based on status
                when (item.status.lowercase()) {
                    "pending" -> {
                        btnStatus.setBackgroundResource(R.drawable.rounded_button_black)
                        btnStatus.backgroundTintList = itemView.context.getColorStateList(R.color.orange)
                    }
                    "approved" -> {
                        btnStatus.setBackgroundResource(R.drawable.rounded_button_black)
                        btnStatus.backgroundTintList = itemView.context.getColorStateList(R.color.green)
                    }
                    "rejected", "denied" -> {
                        btnStatus.setBackgroundResource(R.drawable.rounded_button_black)
                        btnStatus.backgroundTintList = itemView.context.getColorStateList(R.color.red)
                    }
                    "lost" -> {
                        btnStatus.setBackgroundResource(R.drawable.rounded_button_gray)
                        btnStatus.backgroundTintList = itemView.context.getColorStateList(R.color.dark_gray)
                    }
                    "found" -> {
                        btnStatus.setBackgroundResource(R.drawable.rounded_button_black)
                        btnStatus.backgroundTintList = itemView.context.getColorStateList(R.color.primary_teal)
                    }
                    "claimed" -> {
                        btnStatus.setBackgroundResource(R.drawable.rounded_button_black)
                        btnStatus.backgroundTintList = itemView.context.getColorStateList(R.color.blue)
                    }
                    "returned" -> {
                        btnStatus.setBackgroundResource(R.drawable.rounded_button_black)
                        btnStatus.backgroundTintList = itemView.context.getColorStateList(R.color.green)
                    }
                    else -> {
                        btnStatus.setBackgroundResource(R.drawable.rounded_button_gray)
                        btnStatus.backgroundTintList = null
                    }
                }

                // Load image
                Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(ivItemImage)

                // Set click listener
                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    class MyListDiffCallback : DiffUtil.ItemCallback<MyListItem>() {
        override fun areItemsTheSame(oldItem: MyListItem, newItem: MyListItem): Boolean {
            return oldItem.id == newItem.id && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: MyListItem, newItem: MyListItem): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Data model for My List items
 */
data class MyListItem(
    val id: String,
    val title: String,
    val status: String,
    val imageUrl: String?,
    val type: ItemType,
    val createdAt: String
)

enum class ItemType {
    CLAIM,
    LOST_POST,
    FOUND_POST
}