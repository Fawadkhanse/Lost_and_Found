package com.example.lostandfound.feature.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentPostRequestsBinding
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.claimitem.ClaimViewModel
import com.example.lostandfound.feature.item.ItemViewModel
import com.example.lostandfound.utils.AdminBottomNavigationHelper
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Post Requests Fragment - Admin view for verifying pending posts
 * Shows lost and found items that need admin verification
 */
class PostRequestsFragment : BaseFragment() {

    private var _binding: FragmentPostRequestsBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val claimViewModel: ClaimViewModel by viewModel()
    private lateinit var bottomNavHelper: AdminBottomNavigationHelper

    private lateinit var lostItemsAdapter: PostRequestsAdapter
    private lateinit var foundItemsAdapter: PostRequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerViews()
        setupBottomNavigation()
        setupListeners()
        observeViewModels()
        loadPendingPosts()
    }

    private fun setupViews() {
        binding.swipeRefresh.setOnRefreshListener {
            loadPendingPosts()
        }
    }

    private fun setupRecyclerViews() {
        // Lost Items RecyclerView
        lostItemsAdapter = PostRequestsAdapter(
            onApprove = { itemId -> approvePost(itemId, true) },
            onReject = { itemId -> rejectPost(itemId, true) },
            onView = { itemId -> viewPostDetail(itemId, true) }
        )

        binding.rvPendingLostItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lostItemsAdapter
            setHasFixedSize(true)
        }

        // Found Items RecyclerView
        foundItemsAdapter = PostRequestsAdapter(
            onApprove = { itemId -> approvePost(itemId, false) },
            onReject = { itemId -> rejectPost(itemId, false) },
            onView = { itemId -> viewPostDetail(itemId, false) }
        )

        binding.rvPendingFoundItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foundItemsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavHelper = AdminBottomNavigationHelper(binding.bottomNavAdmin)

        bottomNavHelper.setOnTabSelectedListener { tab ->
            when (tab) {
                AdminBottomNavigationHelper.Tab.HOME -> navigateToHome()
                AdminBottomNavigationHelper.Tab.POST_REQUESTS -> {
                    // Already here
                }
                AdminBottomNavigationHelper.Tab.PROFILE -> navigateToProfile()
            }
        }

        // Set Post Requests as selected
        bottomNavHelper.selectTab(AdminBottomNavigationHelper.Tab.POST_REQUESTS)
    }

    private fun setupListeners() {
        // Tab switching between Lost and Found
        binding.btnLostItems.setOnClickListener {
            selectTab(true)
        }

        binding.btnFoundItems.setOnClickListener {
            selectTab(false)
        }
    }

    private fun observeViewModels() {
        // Observe Lost Items
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.lostItemsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading pending lost items...")
                        binding.swipeRefresh.isRefreshing = true
                    }
                    is Resource.Success -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false

                        // Filter only unverified items
                        val pendingItems = resource.data.results.filter { !it.isVerified }

                        // Convert to PostRequestItem
                        val items = pendingItems.map { item ->
                            PostRequestItem(
                                id = item.id,
                                title = item.title,
                                description = item.description,
                                category = item.categoryName,
                                date = item.lostDate,
                                location = item.lostLocation,
                                imageUrl = item.itemImage,
                                user = item.user,
                                isVerified = item.isVerified
                            )
                        }

                        lostItemsAdapter.submitList(items)
                        updateEmptyState()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        showError("Failed to load: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }

        // Observe Found Items
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.foundItemsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading pending found items...")
                        binding.swipeRefresh.isRefreshing = true
                    }
                    is Resource.Success -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false

                        // Filter only unverified items
                        val pendingItems = resource.data.results.filter { !it.isVerified }

                        // Convert to PostRequestItem
                        val items = pendingItems.map { item ->
                            PostRequestItem(
                                id = item.id,
                                title = item.title,
                                description = item.description,
                                category = item.categoryName,
                                date = item.foundDate,
                                location = item.foundLocation,
                                imageUrl = item.imageUrl ?: item.itemImage,
                                user = item.user,
                                isVerified = item.isVerified
                            )
                        }

                        foundItemsAdapter.submitList(items)
                        updateEmptyState()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        showError("Failed to load: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun loadPendingPosts() {
        itemViewModel.getAllLostItems()
        itemViewModel.getAllFoundItems()
    }

    private fun selectTab(isLostItems: Boolean) {
        if (isLostItems) {
            // Show lost items
            binding.layoutLostItems.visibility = View.VISIBLE
            binding.layoutFoundItems.visibility = View.GONE

            // Update button styles
            binding.btnLostItems.setBackgroundResource(R.drawable.rounded_button_black)
            binding.btnLostItems.backgroundTintList =
                requireContext().getColorStateList(R.color.primary_teal)
            binding.btnFoundItems.setBackgroundResource(R.drawable.rounded_button_gray)
            binding.btnFoundItems.backgroundTintList = null
        } else {
            // Show found items
            binding.layoutLostItems.visibility = View.GONE
            binding.layoutFoundItems.visibility = View.VISIBLE

            // Update button styles
            binding.btnFoundItems.setBackgroundResource(R.drawable.rounded_button_black)
            binding.btnFoundItems.backgroundTintList =
                requireContext().getColorStateList(R.color.primary_teal)
            binding.btnLostItems.setBackgroundResource(R.drawable.rounded_button_gray)
            binding.btnLostItems.backgroundTintList = null
        }
    }

    private fun updateEmptyState() {
        // Lost items empty state
        if (lostItemsAdapter.currentList.isEmpty()) {
            binding.tvNoLostItems.visibility = View.VISIBLE
            binding.rvPendingLostItems.visibility = View.GONE
        } else {
            binding.tvNoLostItems.visibility = View.GONE
            binding.rvPendingLostItems.visibility = View.VISIBLE
        }

        // Found items empty state
        if (foundItemsAdapter.currentList.isEmpty()) {
            binding.tvNoFoundItems.visibility = View.VISIBLE
            binding.rvPendingFoundItems.visibility = View.GONE
        } else {
            binding.tvNoFoundItems.visibility = View.GONE
            binding.rvPendingFoundItems.visibility = View.VISIBLE
        }
    }

    private fun approvePost(itemId: String, isLostItem: Boolean) {
        showInfo("Approving post: $itemId")
        // TODO: Implement API call to verify/approve post
        // Call API endpoint: VERIFY_LOST_ITEM or VERIFY_FOUND_ITEM
    }

    private fun rejectPost(itemId: String, isLostItem: Boolean) {
        showInfo("Rejecting post: $itemId")

        // TODO: Implement rejection logic
    }

    private fun viewPostDetail(itemId: String, isLostItem: Boolean) {
        showInfo("Viewing post detail: $itemId")
        // TODO: Navigate to item detail fragment
    }

    private fun navigateToHome() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun navigateToProfile() {
        showInfo("Navigate to Profile")
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNavHelper.isInitialized) {
            bottomNavHelper.selectTab(AdminBottomNavigationHelper.Tab.POST_REQUESTS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Data class for post request items
 */
data class PostRequestItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val date: String,
    val location: String,
    val imageUrl: String?,
    val user: String,
    val isVerified: Boolean
)