// app/src/main/java/com/example/lostandfound/feature/home/ResidentHomeFragment.kt
package com.example.lostandfound.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentResidentBinding
import com.example.lostandfound.domain.auth.LostItemResponse
import com.example.lostandfound.domain.item.FoundItemResponse
import com.example.lostandfound.feature.auth.AuthViewModel
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.item.ItemViewModel
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Resident Home Fragment
 * Displays lost and found items on the home screen with bottom navigation
 */
class ResidentHomeFragment : BaseFragment() {

    private var _binding: FragmentResidentBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val authViewModel: AuthViewModel by viewModel()

    private lateinit var itemsAdapter: ItemsAdapter
    private val allItems = mutableListOf<ItemModel>()

    private var currentSelectedNavItem: BottomNavItem = BottomNavItem.HOME

    enum class BottomNavItem {
        HOME, MESSAGES, MY_POSTS, ACCOUNT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
        setupRecyclerView()
        setupListeners()
        observeViewModels()
        setView()
        loadData()
        highlightBottomNavItem(BottomNavItem.HOME)
    }

    /**
     * Handle back press - show exit confirmation
     */
    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmation()
                }
            }
        )
    }

    private fun showExitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                requireActivity().finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun setView() {
        binding.tvWelcome.text = "Hello ${AuthData.fullName}"
    }

    private fun setupRecyclerView() {
        itemsAdapter = ItemsAdapter { item ->
            onItemClicked(item)
        }

        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        // Back button - show exit confirmation instead of navigating back
        binding.btnBack.setOnClickListener {
            showExitConfirmation()
        }

        // Menu button - show logout option
        binding.btnMenu.setOnClickListener {
            showLogoutConfirmation()
        }

        // Search
        binding.etSearch.setOnClickListener {
            showInfo("Search feature coming soon")
        }

        // Load more button
        binding.tvLoadMore.setOnClickListener {
            loadMoreItems()
        }

        // Category filters
        binding.btnClothes.setOnClickListener {
            filterByCategory("Clothes")
        }

        binding.btnElectronics.setOnClickListener {
            filterByCategory("Electronics")
        }

        // Bottom navigation
        setupBottomNavigation()

        // FAB Add button
        binding.fabAdd.setOnClickListener {
            navigateTo(R.id.action_residentHomeFragment_to_addItemFragment)
        }
    }

    private fun setupBottomNavigation() {
        // Home - Scroll to top if already on home
        binding.bottomNav.navHome.setOnClickListener {
            if (currentSelectedNavItem == BottomNavItem.HOME) {
                // Already on home, scroll to top
                binding.rvPosts.smoothScrollToPosition(0)
            }
            highlightBottomNavItem(BottomNavItem.HOME)
        }

        // Messages
        binding.bottomNav.navMessage.setOnClickListener {
            highlightBottomNavItem(BottomNavItem.MESSAGES)
            navigateTo(R.id.action_residentHomeFragment_to_messagesFragment)
        }

        // My Posts
        binding.bottomNav.navMyPost.setOnClickListener {
            highlightBottomNavItem(BottomNavItem.MY_POSTS)
            navigateTo(R.id.action_residentHomeFragment_to_myListFragment)
        }

        // Account
        binding.bottomNav.navAccount.setOnClickListener {
            highlightBottomNavItem(BottomNavItem.ACCOUNT)
            navigateTo(R.id.action_residentHomeFragment_to_personalInfoFragment)
        }
    }

    /**
     * Highlight selected bottom navigation item
     */
    private fun highlightBottomNavItem(item: BottomNavItem) {
        currentSelectedNavItem = item

        // Reset all items to default state
        resetBottomNavItems()

        // Highlight selected item
        when (item) {
            BottomNavItem.HOME -> {
                binding.bottomNav.navHome.alpha = 1.0f
                // Note: You'll need to add IDs to the ImageView and TextView in layout_bottom_navigation.xml
            }
            BottomNavItem.MESSAGES -> {
                binding.bottomNav.navMessage.alpha = 1.0f
            }
            BottomNavItem.MY_POSTS -> {
                binding.bottomNav.navMyPost.alpha = 1.0f
            }
            BottomNavItem.ACCOUNT -> {
                binding.bottomNav.navAccount.alpha = 1.0f
            }
        }
    }

    /**
     * Reset all bottom navigation items to default state
     */
    private fun resetBottomNavItems() {
        binding.bottomNav.navHome.alpha = 0.6f
        binding.bottomNav.navMessage.alpha = 0.6f
        binding.bottomNav.navMyPost.alpha = 0.6f
        binding.bottomNav.navAccount.alpha = 0.6f
    }

    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        // Clear auth data
        authViewModel.logout()
        AuthData.clearAuthData()

        // Navigate to login and clear back stack
        try {
            findNavController().navigate(
                R.id.action_residentHomeFragment_to_loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.residentHomeFragment, true)
                    .build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showSuccess("Logged out successfully")
    }

    private fun observeViewModels() {
        // Observe Lost Items
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.lostItemsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading lost items...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        val lostItems = resource.data.results.map { it.toItemModel(false) }
                        allItems.addAll(lostItems)
                        updateRecyclerView()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load lost items: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }

        // Observe Found Items
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.foundItemsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading found items...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        val foundItems = resource.data.results.map { it.toItemModel(true) }
                        allItems.addAll(foundItems)
                        updateRecyclerView()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load found items: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }

        // Observe User Dashboard
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.userDashboardState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        updateWelcomeMessage()
                    }
                    is Resource.Error -> {
                        // Handle error silently
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadData() {
        // Clear existing items
        allItems.clear()

        // Load both lost and found items
        itemViewModel.getAllLostItems()
        itemViewModel.getAllFoundItems()

        // Load user dashboard data
        dashboardViewModel.getUserDashboard()
    }

    private fun loadMoreItems() {
        showInfo("Loading more items...")
        loadData()
    }

    private fun updateRecyclerView() {
        // Sort items by date (newest first)
        val sortedItems = allItems.sortedByDescending { it.date }
        itemsAdapter.submitList(sortedItems)

        // Update UI state
        if (sortedItems.isEmpty()) {
            binding.rvPosts.visibility = View.GONE
            binding.tvLoadMore.visibility = View.GONE
            showInfo("No items found")
        } else {
            binding.rvPosts.visibility = View.VISIBLE
            binding.tvLoadMore.visibility = View.VISIBLE
        }
    }

    private fun updateWelcomeMessage() {
        // Update welcome text is already set in setView()
    }

    private fun filterByCategory(category: String) {
        val filteredItems = allItems.filter { it.categoryName.equals(category, ignoreCase = true) }
        itemsAdapter.submitList(filteredItems)
        showInfo("Filtered by $category")
    }

    private fun onItemClicked(item: ItemModel) {
        val bundle = Bundle().apply {
            putString("itemId", item.id)
            putString("itemType", if (item.isFound) "FOUND" else "LOST")
        }
        navigateTo(R.id.action_residentHomeFragment_to_itemDetailFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        // Re-highlight home when returning to this fragment
        highlightBottomNavItem(BottomNavItem.HOME)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Extension functions to convert API models to ItemModel
    private fun LostItemResponse.toItemModel(isFound: Boolean = false): ItemModel {
        return ItemModel(
            id = this.id,
            title = this.title,
            categoryName = this.categoryName,
            date = this.lostDate,
            location = this.lostLocation,
            imageUrl = this.itemImage,
            isFound = isFound,
            status = this.status
        )
    }

    private fun FoundItemResponse.toItemModel(isFound: Boolean = true): ItemModel {
        return ItemModel(
            id = this.id,
            title = this.title,
            categoryName = this.categoryName,
            date = this.foundDate,
            location = this.foundLocation,
            imageUrl = this.imageUrl ?: this.itemImage,
            isFound = isFound,
            status = this.status
        )
    }
}