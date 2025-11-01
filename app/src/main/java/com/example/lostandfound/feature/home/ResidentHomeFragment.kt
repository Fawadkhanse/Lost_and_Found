// app/src/main/java/com/example/lostandfound/feature/home/ResidentHomeFragment.kt
package com.example.lostandfound.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentResidentBinding
import com.example.lostandfound.domain.auth.LostItemResponse
import com.example.lostandfound.domain.item.FoundItemResponse
import com.example.lostandfound.feature.auth.AuthViewModel
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.item.ItemViewModel
import com.example.lostandfound.utils.AuthData
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Resident Home Fragment with Navigation Drawer
 * Displays lost and found items on the home screen
 */
class ResidentHomeFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: FragmentResidentBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val authViewModel: AuthViewModel by viewModel()

    private lateinit var itemsAdapter: ItemsAdapter
    private val allItems = mutableListOf<ItemModel>()

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
        setupNavigationDrawer()
        setupRecyclerView()
        setupListeners()
        observeViewModels()
        setView()
        loadData()
    }

    private fun setupNavigationDrawer() {
        // Set up navigation item selected listener
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Update navigation header with user info
        val headerView = binding.navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<android.widget.TextView>(R.id.tvUserName)
        val tvUserEmail = headerView.findViewById<android.widget.TextView>(R.id.tvUserEmail)
        val ivProfileImage = headerView.findViewById<android.widget.ImageView>(R.id.ivProfileImage)

        // Set user data
        AuthData.userDetailInfo?.let { user ->
            tvUserName.text = "${user.firstName} ${user.lastName}"
            tvUserEmail.text = user.email

            // Load profile image if available
            if (!user.profileImage.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .circleCrop()
                    .into(ivProfileImage)
            }
        }
    }

    private fun setView() {
        binding.tvWelcome.text = "Hello " + AuthData.fullName
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
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Menu button - opens drawer
        binding.btnMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Search
        binding.etSearch.setOnClickListener {
            Toast.makeText(requireContext(), "Search clicked", Toast.LENGTH_SHORT).show()
        }

        // Load more button
        binding.tvLoadMore.setOnClickListener {
            loadMoreItems()
        }

        // Category buttons
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
        // Home - Already on home, do nothing or scroll to top
        binding.bottomNav.navHome.setOnClickListener {
            // Scroll to top of the list
            binding.rvPosts.smoothScrollToPosition(0)
        }

        // Messages
        binding.bottomNav.navMessage.setOnClickListener {
            // Navigate to messages fragment
            Toast.makeText(requireContext(), "Messages coming soon", Toast.LENGTH_SHORT).show()
            // TODO: Uncomment when messages fragment is ready
            // navigateTo(R.id.action_residentHomeFragment_to_messagesFragment)
        }

        // My Posts
        binding.bottomNav.navMyPost.setOnClickListener {
            // Navigate to my list fragment
            navigateTo(R.id.action_residentHomeFragment_to_myListFragment)
        }

        // Account
        binding.bottomNav.navAccount.setOnClickListener {
            // Navigate to profile
            navigateTo(R.id.action_residentHomeFragment_to_personalInfoFragment)
        }
    }

    /**
     * Navigate to destination with proper back stack handling
     * Prevents adding duplicate entries to back stack
     */
    private fun navigateTo(actionId: Int) {
        try {
            findNavController().navigate(actionId)
        } catch (e: Exception) {
            // Navigation action not found or already navigated
            e.printStackTrace()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                // Navigate to profile
                navigateTo(R.id.action_residentHomeFragment_to_personalInfoFragment)
            }
            R.id.nav_logout -> {
                // Show logout confirmation
                showLogoutConfirmation()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
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

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(requireContext(), "Loading more items...", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(requireContext(), "Filtered by $category", Toast.LENGTH_SHORT).show()
    }

    private fun onItemClicked(item: ItemModel) {
        Toast.makeText(requireContext(), "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to item detail
        // val bundle = Bundle().apply {
        //     putString("itemId", item.id)
        //     putString("itemType", if (item.isFound) "FOUND" else "LOST")
        // }
        // navigateTo(R.id.action_residentHomeFragment_to_itemDetailFragment)
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