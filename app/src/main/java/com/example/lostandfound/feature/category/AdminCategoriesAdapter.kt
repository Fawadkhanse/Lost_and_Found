package com.example.lostandfound.feature.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ItemCategoryAdminBinding
import com.example.lostandfound.domain.auth.CategoryResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying categories in admin section
 */
class AdminCategoriesAdapter(
    private val onEditClick: (CategoryResponse) -> Unit,
    private val onDeleteClick: (CategoryResponse) -> Unit
) : ListAdapter<CategoryResponse, AdminCategoriesAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryResponse) {
            binding.apply {
                // Set category name
                tvCategoryName.text = category.name ?: "Unknown"

                // Set category description
                tvCategoryDescription.text = category.description ?: "No description"

                // Set created date
                category.createdAt?.let {
                    tvCreatedDate.text = "Created: ${formatDate(it)}"
                } ?: run {
                    tvCreatedDate.text = "Created: Unknown"
                }

                // Set category icon based on name
                val iconRes = getCategoryIcon(category.name ?: "")
                ivCategoryIcon.setImageResource(iconRes)

                // Edit button click
                btnEdit.setOnClickListener {
                    onEditClick(category)
                }

                // Delete button click
                btnDelete.setOnClickListener {
                    onDeleteClick(category)
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateString)

                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                try {
                    // Fallback format
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = inputFormat.parse(dateString)
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    date?.let { outputFormat.format(it) } ?: dateString
                } catch (e: Exception) {
                    dateString
                }
            }
        }

        private fun getCategoryIcon(categoryName: String): Int {
            return when (categoryName.lowercase()) {
                "wallets" -> R.drawable.ic_clothes
                else -> R.drawable.ic_clothes
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryResponse>() {
        override fun areItemsTheSame(oldItem: CategoryResponse, newItem: CategoryResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryResponse, newItem: CategoryResponse): Boolean {
            return oldItem == newItem
        }
    }
}
