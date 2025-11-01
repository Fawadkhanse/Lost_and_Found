package com.example.lostandfound.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.lostandfound.R
import com.example.lostandfound.databinding.LayoutBottomNavigationAdminBinding

/**
 * Helper class for managing Admin Bottom Navigation
 * Handles selection state and badge count
 */
class AdminBottomNavigationHelper(
    private val binding: LayoutBottomNavigationAdminBinding
) {

    enum class Tab {
        HOME, POST_REQUESTS, PROFILE
    }

    private var currentTab: Tab = Tab.HOME
    private var onTabSelectedListener: ((Tab) -> Unit)? = null

    init {
        setupClickListeners()
        selectTab(Tab.HOME) // Set initial state
    }

    /**
     * Setup click listeners for all navigation items
     */
    private fun setupClickListeners() {
        binding.navHome.setOnClickListener {
            selectTab(Tab.HOME)
            onTabSelectedListener?.invoke(Tab.HOME)
        }

        binding.navPostRequests.setOnClickListener {
            selectTab(Tab.POST_REQUESTS)
            onTabSelectedListener?.invoke(Tab.POST_REQUESTS)
        }

        binding.navProfile.setOnClickListener {
            selectTab(Tab.PROFILE)
            onTabSelectedListener?.invoke(Tab.PROFILE)
        }
    }

    /**
     * Select a specific tab and update UI
     */
    fun selectTab(tab: Tab) {
        if (currentTab == tab) return

        currentTab = tab

        // Reset all tabs to default state
        resetAllTabs()

        // Highlight selected tab
        when (tab) {
            Tab.HOME -> {
                setTabSelected(
                    binding.ivHomeIcon,
                    binding.tvHomeLabel
                )
            }
            Tab.POST_REQUESTS -> {
                setTabSelected(
                    binding.ivPostRequestsIcon,
                    binding.tvPostRequestsLabel
                )
            }
            Tab.PROFILE -> {
                setTabSelected(
                    binding.ivProfileIcon,
                    binding.tvProfileLabel
                )
            }
        }
    }

    /**
     * Reset all tabs to unselected state
     */
    private fun resetAllTabs() {
        setTabUnselected(binding.ivHomeIcon, binding.tvHomeLabel)
        setTabUnselected(binding.ivPostRequestsIcon, binding.tvPostRequestsLabel)
        setTabUnselected(binding.ivProfileIcon, binding.tvProfileLabel)
    }

    /**
     * Set tab to selected state
     */
    private fun setTabSelected(icon: ImageView, label: TextView) {
        icon.setColorFilter(
            ContextCompat.getColor(icon.context, R.color.primary_teal)
        )
        label.setTextColor(
            ContextCompat.getColor(label.context, R.color.primary_teal)
        )
        label.textSize = 11f
        label.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    /**
     * Set tab to unselected state
     */
    private fun setTabUnselected(icon: ImageView, label: TextView) {
        icon.setColorFilter(
            ContextCompat.getColor(icon.context, R.color.dark_gray)
        )
        label.setTextColor(
            ContextCompat.getColor(label.context, R.color.dark_gray)
        )
        label.textSize = 11f
        label.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    /**
     * Update the badge count for post requests
     * @param count The number of pending requests (0 to hide badge)
     */
    fun updateBadgeCount(count: Int) {
        if (count > 0) {
            binding.tvBadgeCount.visibility = View.VISIBLE
            binding.tvBadgeCount.text = when {
                count > 99 -> "99+"
                else -> count.toString()
            }
        } else {
            binding.tvBadgeCount.visibility = View.GONE
        }
    }

    /**
     * Set listener for tab selection
     */
    fun setOnTabSelectedListener(listener: (Tab) -> Unit) {
        this.onTabSelectedListener = listener
    }

    /**
     * Get current selected tab
     */
    fun getCurrentTab(): Tab = currentTab
}