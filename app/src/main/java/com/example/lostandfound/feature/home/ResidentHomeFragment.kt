package com.example.lostandfound.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentResidentBinding
import com.example.lostandfound.domain.auth.LostItemResponse
import com.example.lostandfound.domain.item.FoundItemResponse
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.item.ItemViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Resident Home Fragment
 * Displays lost and found items on the home screen
 */
class ResidentHomeFragment : BaseFragment() {

    private var _binding: FragmentResidentBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()

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
        setupRecyclerView()
        setupListeners()
        observeViewModels()
        loadData()
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

        // Menu button
        binding.btnMenu.setOnClickListener {
            // Navigate to menu or show options
            Toast.makeText(requireContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        // Search
        binding.etSearch.setOnClickListener {
            // Navigate to search screen
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
        binding.bottomNav.navHome.setOnClickListener {
            // Already on home
        }

        binding.bottomNav.navMessage.setOnClickListener {
            // Navigate to messages/chat
            // findNavController().navigate(R.id.action_residentHomeFragment_to_chatFragment)
            Toast.makeText(requireContext(), "Messages", Toast.LENGTH_SHORT).show()
        }

        binding.bottomNav.navAccount.setOnClickListener {
            // Navigate to account/profile
            // findNavController().navigate(R.id.action_residentHomeFragment_to_profileFragment)
            Toast.makeText(requireContext(), "Account", Toast.LENGTH_SHORT).show()
        }
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
                        // Handle error silently or show a toast
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
        // Implement pagination if needed
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
        // Update welcome text with user info
        // For now using default from layout
        // You can get user info from AuthViewModel if needed
    }

    private fun filterByCategory(category: String) {
        val filteredItems = allItems.filter { it.categoryName.equals(category, ignoreCase = true) }
        itemsAdapter.submitList(filteredItems)
        Toast.makeText(requireContext(), "Filtered by $category", Toast.LENGTH_SHORT).show()
    }

    private fun onItemClicked(item: ItemModel) {
        // Navigate to item detail screen
        Toast.makeText(requireContext(), "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()

        // Example navigation:
        // val bundle = Bundle().apply {
        //     putString("itemId", item.id)
        //     putBoolean("isFound", item.isFound)
        // }
        // findNavController().navigate(
        //     R.id.action_residentHomeFragment_to_itemDetailFragment,
        //     bundle
        // )
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