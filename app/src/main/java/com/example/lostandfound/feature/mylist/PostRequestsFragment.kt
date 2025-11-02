package com.example.lostandfound.feature.mylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
 * Shows lost items, found items, and claims that need admin verification
 * Enhanced with claim approval/rejection and admin notes
 */
class PostRequestsFragment : BaseFragment() {

    private var _binding: FragmentPostRequestsBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val claimViewModel: ClaimViewModel by viewModel()
    private lateinit var bottomNavHelper: AdminBottomNavigationHelper

    private lateinit var lostItemsAdapter: PostRequestsAdapter
    private lateinit var foundItemsAdapter: PostRequestsAdapter
    private lateinit var claimsAdapter: ClaimsAdapter

    private var currentTab = TabType.LOST_ITEMS

    enum class TabType {
        LOST_ITEMS, FOUND_ITEMS, CLAIMS
    }

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

        // Claims RecyclerView
        claimsAdapter = ClaimsAdapter(
            onApprove = { claimId -> showApproveClaimDialog(claimId) },
            onReject = { claimId -> showRejectClaimDialog(claimId) },
            onView = { claimId -> viewClaimDetail(claimId) }
        )

        binding.rvPendingClaims.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = claimsAdapter
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
        // Tab switching between Lost, Found, and Claims
        binding.btnLostItems.setOnClickListener {
            selectTab(TabType.LOST_ITEMS)
        }

        binding.btnFoundItems.setOnClickListener {
            selectTab(TabType.FOUND_ITEMS)
        }

        binding.btnClaims.setOnClickListener {
            selectTab(TabType.CLAIMS)
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
                        updateBadgeCount()
                        updateEmptyState()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        showError("Failed to load lost items: ${resource.exception.message}")
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
                        updateBadgeCount()
                        updateEmptyState()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        showError("Failed to load found items: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }

        // Observe Claims
        viewLifecycleOwner.lifecycleScope.launch {
            claimViewModel.claimsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading pending claims...")
                        binding.swipeRefresh.isRefreshing = true
                    }
                    is Resource.Success -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false

                        // Filter only pending claims
                        val pendingClaims = resource.data.results.filter {
                            it.status.equals("pending", ignoreCase = true)
                        }

                        // Convert to ClaimItem
                        val items = pendingClaims.map { claim ->
                            ClaimItem(
                                id = claim.id,
                                foundItemTitle = claim.foundItemTitle,
                                foundItemImage = claim.foundItemImage,
                                claimDescription = claim.claimDescription,
                                proofOfOwnership = claim.proofOfOwnership,
                                userEmail = claim.userEmail,
                                status = claim.status,
                                createdAt = claim.createdAt,
                                adminNotes = claim.adminNotes
                            )
                        }

                        claimsAdapter.submitList(items)
                        updateBadgeCount()
                        updateEmptyState()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        showError("Failed to load claims: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }

        // Observe Verify Lost Item
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.verifyLostItemState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Approving lost item...")
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Lost item approved successfully!")
                        itemViewModel.resetVerifyLostItemState()
                        loadPendingPosts()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to approve: ${resource.exception.message}")
                        itemViewModel.resetVerifyLostItemState()
                    }
                    Resource.None -> hideLoading()
                }
            }
        }

        // Observe Verify Found Item
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.verifyFoundItemState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Approving found item...")
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Found item approved successfully!")
                        itemViewModel.resetVerifyFoundItemState()
                        loadPendingPosts()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to approve: ${resource.exception.message}")
                        itemViewModel.resetVerifyFoundItemState()
                    }
                    Resource.None -> hideLoading()
                }
            }
        }

        // Observe Reject Lost Item
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.rejectLostItemState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Rejecting lost item...")
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Lost item rejected successfully!")
                        itemViewModel.resetRejectLostItemState()
                        loadPendingPosts()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to reject: ${resource.exception.message}")
                        itemViewModel.resetRejectLostItemState()
                    }
                    Resource.None -> hideLoading()
                }
            }
        }

        // Observe Reject Found Item
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.rejectFoundItemState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Rejecting found item...")
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Found item rejected successfully!")
                        itemViewModel.resetRejectFoundItemState()
                        loadPendingPosts()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to reject: ${resource.exception.message}")
                        itemViewModel.resetRejectFoundItemState()
                    }
                    Resource.None -> hideLoading()
                }
            }
        }

        // Observe Update Claim
        viewLifecycleOwner.lifecycleScope.launch {
            claimViewModel.updateClaimState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Updating claim...")
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Claim updated successfully!")
                        claimViewModel.resetUpdateState()
                        loadPendingPosts()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to update claim: ${resource.exception.message}")
                        claimViewModel.resetUpdateState()
                    }
                    Resource.None -> hideLoading()
                }
            }
        }
    }

    private fun loadPendingPosts() {
        itemViewModel.getAllLostItems()
        itemViewModel.getAllFoundItems()
        claimViewModel.getAllClaims()
    }

    private fun selectTab(tab: TabType) {
        currentTab = tab

        // Hide all layouts
        binding.layoutLostItems.visibility = View.GONE
        binding.layoutFoundItems.visibility = View.GONE
        binding.layoutClaims.visibility = View.GONE

        // Reset all button styles
        binding.btnLostItems.setBackgroundResource(R.drawable.rounded_button_gray)
        binding.btnLostItems.backgroundTintList = null
        binding.btnFoundItems.setBackgroundResource(R.drawable.rounded_button_gray)
        binding.btnFoundItems.backgroundTintList = null
        binding.btnClaims.setBackgroundResource(R.drawable.rounded_button_gray)
        binding.btnClaims.backgroundTintList = null

        when (tab) {
            TabType.LOST_ITEMS -> {
                binding.layoutLostItems.visibility = View.VISIBLE
                binding.btnLostItems.setBackgroundResource(R.drawable.rounded_button_black)
                binding.btnLostItems.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.primary_teal)
            }
            TabType.FOUND_ITEMS -> {
                binding.layoutFoundItems.visibility = View.VISIBLE
                binding.btnFoundItems.setBackgroundResource(R.drawable.rounded_button_black)
                binding.btnFoundItems.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.primary_teal)
            }
            TabType.CLAIMS -> {
                binding.layoutClaims.visibility = View.VISIBLE
                binding.btnClaims.setBackgroundResource(R.drawable.rounded_button_black)
                binding.btnClaims.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.primary_teal)
            }
        }

        updateEmptyState()
    }

    private fun updateBadgeCount() {
        val lostCount = lostItemsAdapter.currentList.size
        val foundCount = foundItemsAdapter.currentList.size
        val claimsCount = claimsAdapter.currentList.size
        val totalCount = lostCount + foundCount + claimsCount

        bottomNavHelper.updateBadgeCount(totalCount)
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

        // Claims empty state
        if (claimsAdapter.currentList.isEmpty()) {
            binding.tvNoClaims.visibility = View.VISIBLE
            binding.rvPendingClaims.visibility = View.GONE
        } else {
            binding.tvNoClaims.visibility = View.GONE
            binding.rvPendingClaims.visibility = View.VISIBLE
        }
    }

    // ============================================
    // Lost/Found Items Actions
    // ============================================

    private fun approvePost(itemId: String, isLostItem: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle("Approve Post")
            .setMessage("Are you sure you want to approve this ${if (isLostItem) "lost" else "found"} item?")
            .setPositiveButton("Approve") { _, _ ->
                if (isLostItem) {
                    itemViewModel.verifyLostItem(itemId)
                } else {
                    itemViewModel.verifyFoundItem(itemId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun rejectPost(itemId: String, isLostItem: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle("Reject Post")
            .setMessage("Are you sure you want to reject this ${if (isLostItem) "lost" else "found"} item? This will delete the post.")
            .setPositiveButton("Reject") { _, _ ->
                if (isLostItem) {
                    itemViewModel.rejectLostItem(itemId)
                } else {
                    itemViewModel.rejectFoundItem(itemId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun viewPostDetail(itemId: String, isLostItem: Boolean) {
        // Navigate to item detail
        val bundle = Bundle().apply {
            putString("itemId", itemId)
            putString("itemType", if (isLostItem) "LOST" else "FOUND")
        }
     //   navigateTo(R.id.action_postRequestsFragment_to_itemDetailFragment, bundle)
    }

    // ============================================
    // Claims Actions
    // ============================================

    private fun showApproveClaimDialog(claimId: String) {
        // Get claim details
        val claim = claimsAdapter.currentList.find { it.id == claimId } ?: return

        // Create dialog with admin notes input
        val dialogView = layoutInflater.inflate(R.layout.dialog_admin_notes, null)
        val etAdminNotes = dialogView.findViewById<EditText>(R.id.etAdminNotes)

        AlertDialog.Builder(requireContext())
            .setTitle("Approve Claim")
            .setMessage("Approving claim for: ${claim.foundItemTitle}\nUser: ${claim.userEmail}")
            .setView(dialogView)
            .setPositiveButton("Approve") { _, _ ->
                val adminNotes = etAdminNotes.text.toString().trim()
                approveClaim(claimId, claim, adminNotes)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectClaimDialog(claimId: String) {
        // Get claim details
        val claim = claimsAdapter.currentList.find { it.id == claimId } ?: return

        // Create dialog with admin notes input
        val dialogView = layoutInflater.inflate(R.layout.dialog_admin_notes, null)
        val etAdminNotes = dialogView.findViewById<EditText>(R.id.etAdminNotes)
        etAdminNotes.hint = "Reason for rejection (required)"

        AlertDialog.Builder(requireContext())
            .setTitle("Reject Claim")
            .setMessage("Rejecting claim for: ${claim.foundItemTitle}\nUser: ${claim.userEmail}")
            .setView(dialogView)
            .setPositiveButton("Reject") { _, _ ->
                val adminNotes = etAdminNotes.text.toString().trim()
                if (adminNotes.isEmpty()) {
                    showError("Please provide a reason for rejection")
                    return@setPositiveButton
                }
                rejectClaim(claimId, claim, adminNotes)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun approveClaim(claimId: String, claim: ClaimItem, adminNotes: String) {
        // First, get the full claim details to get the foundItem ID
        viewLifecycleOwner.lifecycleScope.launch {
            claimViewModel.getClaimById(claimId)

            // Wait for the claim detail to load
            claimViewModel.claimDetailState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val fullClaim = resource.data

                        // Now update the claim with proper foundItem ID
                        claimViewModel.updateClaim(
                            id = claimId,
                            foundItem = fullClaim.foundItem, // Proper found item ID
                            claimDescription = fullClaim.claimDescription,
                            proofOfOwnership = fullClaim.proofOfOwnership,
                            supportingImages = fullClaim.supportingImages,
                            status = "approved",
                            adminNotes = adminNotes.ifEmpty { "Claim approved by admin" }
                        )

                        // Reset detail state to avoid re-triggering
                        claimViewModel.resetDetailState()
                    }
                    is Resource.Error -> {
                        showError("Failed to fetch claim details: ${resource.exception.message}")
                        claimViewModel.resetDetailState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun rejectClaim(claimId: String, claim: ClaimItem, adminNotes: String) {
        // First, get the full claim details to get the foundItem ID
        viewLifecycleOwner.lifecycleScope.launch {
            claimViewModel.getClaimById(claimId)

            // Wait for the claim detail to load
            claimViewModel.claimDetailState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val fullClaim = resource.data

                        // Now update the claim with proper foundItem ID
                        claimViewModel.updateClaim(
                            id = claimId,
                            foundItem = fullClaim.foundItem, // Proper found item ID
                            claimDescription = fullClaim.claimDescription,
                            proofOfOwnership = fullClaim.proofOfOwnership,
                            supportingImages = fullClaim.supportingImages,
                            status = "rejected",
                            adminNotes = adminNotes
                        )

                        // Reset detail state to avoid re-triggering
                        claimViewModel.resetDetailState()
                    }
                    is Resource.Error -> {
                        showError("Failed to fetch claim details: ${resource.exception.message}")
                        claimViewModel.resetDetailState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun viewClaimDetail(claimId: String) {
        // Get the claim
        val claim = claimsAdapter.currentList.find { it.id == claimId } ?: return

        // Show claim details in a dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_claim_detail, null)
        // TODO: Populate dialog with claim details

        AlertDialog.Builder(requireContext())
            .setTitle("Claim Details")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun navigateToHome() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun navigateToProfile() {
      //  navigateTo(R.id.action_postRequestsFragment_to_personalInfoFragment)
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

/**
 * Data class for claim items
 */
data class ClaimItem(
    val id: String,
    val foundItemTitle: String,
    val foundItemImage: String?,
    val claimDescription: String,
    val proofOfOwnership: String,
    val userEmail: String,
    val status: String,
    val createdAt: String,
    val adminNotes: String?
)