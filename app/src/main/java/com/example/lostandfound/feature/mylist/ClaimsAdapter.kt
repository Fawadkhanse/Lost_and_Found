package com.example.lostandfound.feature.mylist
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ItemClaimRequestBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying claim requests that need admin verification
 */
class ClaimsAdapter(
    private val onApprove: (String) -> Unit,
    private val onReject: (String) -> Unit,
    private val onView: (String) -> Unit
) : ListAdapter<ClaimItem, ClaimsAdapter.ClaimViewHolder>(ClaimDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClaimViewHolder {
        val binding = ItemClaimRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClaimViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClaimViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClaimViewHolder(
        private val binding: ItemClaimRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(claim: ClaimItem) {
            binding.apply {
                // Set claim details
                tvFoundItemTitle.text = "Item: ${claim.foundItemTitle}"
                tvClaimDescription.text = claim.claimDescription
                tvProofOfOwnership.text = "Proof: ${claim.proofOfOwnership}"
                tvUserEmail.text = "Claimant: ${claim.userEmail}"
                tvDate.text = formatDate(claim.createdAt)

                // Show admin notes if available
                if (!claim.adminNotes.isNullOrEmpty()) {
                    tvAdminNotes.visibility = android.view.View.VISIBLE
                    tvAdminNotes.text = "Notes: ${claim.adminNotes}"
                } else {
                    tvAdminNotes.visibility = android.view.View.GONE
                }

                // Load found item image
                if (!claim.foundItemImage.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(claim.foundItemImage)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(ivFoundItemImage)
                } else {
                    ivFoundItemImage.setImageResource(R.drawable.ic_placeholder)
                }

                // Set button listeners
                btnApprove.setOnClickListener {
                    onApprove(claim.id)
                }

                btnReject.setOnClickListener {
                    onReject(claim.id)
                }

                root.setOnClickListener {
                    onView(claim.id)
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateString)

                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    val date = inputFormat.parse(dateString)
                    val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    date?.let { outputFormat.format(it) } ?: dateString
                } catch (e: Exception) {
                    dateString
                }
            }
        }
    }

    class ClaimDiffCallback : DiffUtil.ItemCallback<ClaimItem>() {
        override fun areItemsTheSame(oldItem: ClaimItem, newItem: ClaimItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClaimItem, newItem: ClaimItem): Boolean {
            return oldItem == newItem
        }
    }
}