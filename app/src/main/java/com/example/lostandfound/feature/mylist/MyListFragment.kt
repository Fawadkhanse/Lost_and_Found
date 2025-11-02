package com.example.lostandfound.feature.mylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentMyListBinding
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.item.AddItemFragment
import com.example.lostandfound.feature.claimitem.ClaimViewModel
import com.example.lostandfound.feature.item.ItemViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * MyListFragment - FIXED VERSION
 * Displays user's claims and posts with proper list synchronization
 */
class MyListFragment : BaseFragment() {

    private var _binding: FragmentMyListBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val claimViewModel: ClaimViewModel by viewModel()

    private lateinit var myListAdapter: MyListAdapter
    private var currentTab = Tab.POSTS

    // Track loading states
    private var areClaimsLoaded = false
    private var areLostItemsLoaded = false
    private var areFoundItemsLoaded = false

    enum class Tab {
        CLAIMS, POSTS
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModels()
    }

    override fun onResume() {
        super.onResume()
        // Reload current tab when returning
        when (currentTab) {
            Tab.CLAIMS -> loadClaims()
            Tab.POSTS -> loadPosts()
        }
    }

    private fun setupRecyclerView() {
        myListAdapter = MyListAdapter { item ->
            onItemClicked(item)
        }

        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myListAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnClaims.setOnClickListener {
            selectTab(Tab.CLAIMS)
        }

        binding.btnPosts.setOnClickListener {
            selectTab(Tab.POSTS)
        }
    }

    private fun observeViewModels() {
        // Observe Claims
        viewLifecycleOwner.lifecycleScope.launch {
            claimViewModel.claimsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading claims...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        areClaimsLoaded = true

                        if (currentTab == Tab.CLAIMS) {
                            val claims = resource.data.results.map { claim ->
                                MyListItem(
                                    id = claim.id,
                                    title = claim.foundItemTitle,
                                    status = claim.status.replaceFirstChar { it.uppercase() },
                                    imageUrl = claim.foundItemImage,
                                    type = ItemType.CLAIM,
                                    createdAt = claim.createdAt
                                )
                            }
                            updateList(claims)
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        areClaimsLoaded = true
                        showError("Failed to load claims: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }

        // Observe Lost Items
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.lostItemsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (currentTab == Tab.POSTS) {
                            showLoading("Loading posts...")
                        }
                    }
                    is Resource.Success -> {
                        areLostItemsLoaded = true

                        if (currentTab == Tab.POSTS) {
                            updatePostsList()
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        areLostItemsLoaded = true
                        showError("Failed to load lost items: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        // Initial state
                    }
                }
            }
        }

        // Observe Found Items
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.foundItemsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Already showing loading from lost items
                    }
                    is Resource.Success -> {
                        areFoundItemsLoaded = true

                        if (currentTab == Tab.POSTS) {
                            updatePostsList()
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        areFoundItemsLoaded = true
                        // Error already handled in lost items observer
                    }
                    Resource.None -> {
                        // Initial state
                    }
                }
            }
        }
    }

    private fun updatePostsList() {
        // Only update when both lost and found items are loaded
        if (!areLostItemsLoaded || !areFoundItemsLoaded) {
            return
        }

        hideLoading()

        val allPosts = mutableListOf<MyListItem>()

        // Get lost items
        val lostItemsState = itemViewModel.lostItemsListState.value
        if (lostItemsState is Resource.Success) {
            val lostItems = lostItemsState.data.results.map { item ->
                MyListItem(
                    id = item.id,
                    title = item.title,
                    status = item.status.replaceFirstChar { it.uppercase() },
                    imageUrl = item.itemImage,
                    type = ItemType.LOST_POST,
                    createdAt = item.createdAt
                )
            }
            allPosts.addAll(lostItems)
        }

        // Get found items
        val foundItemsState = itemViewModel.foundItemsListState.value
        if (foundItemsState is Resource.Success) {
            val foundItems = foundItemsState.data.results.map { item ->
                MyListItem(
                    id = item.id,
                    title = item.title,
                    status = item.status.replaceFirstChar { it.uppercase() },
                    imageUrl = item.imageUrl ?: item.itemImage,
                    type = ItemType.FOUND_POST,
                    createdAt = item.createdAt
                )
            }
            allPosts.addAll(foundItems)
        }

        // Sort by creation date (newest first)
        allPosts.sortByDescending { it.createdAt }
        updateList(allPosts)
    }

    private fun selectTab(tab: Tab) {
        currentTab = tab

        // Reset loading states
        areClaimsLoaded = false
        areLostItemsLoaded = false
        areFoundItemsLoaded = false

        when (tab) {
            Tab.CLAIMS -> {
                // Update button styles
                binding.btnClaims.apply {
                    setBackgroundResource(R.drawable.rounded_button_black)
                    backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_teal)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                binding.btnPosts.apply {
                    setBackgroundResource(R.drawable.rounded_button_gray)
                    backgroundTintList = null
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }

                // Load claims data
                loadClaims()
            }
            Tab.POSTS -> {
                // Update button styles
                binding.btnPosts.apply {
                    setBackgroundResource(R.drawable.rounded_button_black)
                    backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_teal)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                binding.btnClaims.apply {
                    setBackgroundResource(R.drawable.rounded_button_gray)
                    backgroundTintList = null
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }

                // Load posts data
                loadPosts()
            }
        }
    }

    private fun loadClaims() {
        claimViewModel.getAllClaims()
    }

    private fun loadPosts() {
        // Load both lost and found items
        itemViewModel.getAllLostItems()
        itemViewModel.getAllFoundItems()
    }

    private fun updateList(items: List<MyListItem>) {
        // Submit new list to trigger DiffUtil
        myListAdapter.submitList(items.toList())

        // Update UI state
        if (items.isEmpty()) {
            binding.rvItems.visibility = View.GONE
            showInfo("No items found")
        } else {
            binding.rvItems.visibility = View.VISIBLE
        }
    }

    private fun onItemClicked(item: MyListItem) {
        when (item.type) {
            ItemType.CLAIM -> {
                // Navigate to claim detail
                Toast.makeText(
                    requireContext(),
                    "Viewing claim: ${item.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ItemType.LOST_POST -> {
                // Navigate to lost item detail
                val bundle = Bundle().apply {
                    putString("itemId", item.id)
                    putString("itemType", "LOST")
                }
                navigateTo(R.id.action_myListFragment_to_itemDetailFragment, bundle)
            }
            ItemType.FOUND_POST -> {
                // Navigate to found item detail
                val bundle = Bundle().apply {
                    putString("itemId", item.id)
                    putString("itemType", "FOUND")
                }
                navigateTo(R.id.action_myListFragment_to_itemDetailFragment, bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}