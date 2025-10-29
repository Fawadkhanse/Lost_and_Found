package com.example.lostandfound.feature.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ItemPostBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter for displaying Lost and Found items
 */
class ItemsAdapter(
    private val onItemClick: (ItemModel) -> Unit
) : ListAdapter<ItemModel, ItemsAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemModel) {
            binding.apply {
                // Set item name
                tvItemName.text = item.title

                // Set category
                tvCategory.text = item.categoryName

                // Set date
                tvDateLost.text = formatDate(item.date)

                // Set location
                tvLocation.text = item.location

                // Set status badge
                tvStatus.text = if (item.isFound) "Found" else "Lost"
                tvStatus.setBackgroundResource(
                    if (item.isFound) R.drawable.rounded_button_black
                    else R.drawable.rounded_button_gray
                )

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
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<ItemModel>() {
        override fun areItemsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Unified data model for Lost and Found items
 */
data class ItemModel(
    val id: String,
    val title: String,
    val categoryName: String,
    val date: String,
    val location: String,
    val imageUrl: String?,
    val isFound: Boolean,
    val status: String
)