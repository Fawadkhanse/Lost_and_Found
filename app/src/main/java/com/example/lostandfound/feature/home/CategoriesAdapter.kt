// app/src/main/java/com/example/lostandfound/feature/home/CategoriesAdapter.kt
package com.example.lostandfound.feature.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ItemCategoryBinding
import com.example.lostandfound.databinding.ItemCategoryHomeBinding

class CategoriesAdapter(
    private val onCategoryClick: (String) -> Unit
) : ListAdapter<String, CategoriesAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategory: String = "All Categories"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryHomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String) {
            binding.tvCategoryName.text = category
            
            // Get category icon based on name
            val iconRes = when (category.lowercase()) {
                "all categories" -> R.drawable.ic_clothes
                "clothes" -> R.drawable.ic_clothes
                "electronics" -> R.drawable.ic_electronics
                "documents" -> R.drawable.ic_clothes
                "accessories" -> R.drawable.ic_clothes
                "books" -> R.drawable.ic_clothes
                "keys" -> R.drawable.ic_key
                "bags" -> R.drawable.ic_clothes
                "phones" -> R.drawable.ic_phone
                "wallets" -> R.drawable.ic_clothes
                else -> R.drawable.ic_clothes
            }
            
            binding.ivCategoryIcon.setImageResource(iconRes)
            
            // Highlight selected category
            if (category == selectedCategory) {
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.primary_teal)
                )
                binding.tvCategoryName.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.ivCategoryIcon.setColorFilter(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
            } else {
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.light_gray)
                )
                binding.tvCategoryName.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.black)
                )
                binding.ivCategoryIcon.setColorFilter(
                    ContextCompat.getColor(binding.root.context, android.R.color.black)
                )
            }

            binding.root.setOnClickListener {
                val previousSelected = selectedCategory
                selectedCategory = category
                onCategoryClick(category)
                
                // Refresh the affected items
                notifyItemChanged(currentList.indexOf(previousSelected))
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
