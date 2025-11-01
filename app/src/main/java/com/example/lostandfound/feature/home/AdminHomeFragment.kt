package com.example.lostandfound.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentAdminHomeBinding
import com.example.lostandfound.domain.auth.ActivityItem
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.utils.AdminBottomNavigationHelper
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Admin Home Fragment - Displays dashboard with statistics and recent activities
 */
class AdminHomeFragment : BaseFragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: DashboardViewModel by viewModel()
    private lateinit var activitiesAdapter: RecentActivitiesAdapter
    private lateinit var bottomNavHelper: AdminBottomNavigationHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        setupBottomNavigation()
        setupListeners()
        observeViewModel()
        loadDashboardData()
    }

    private fun setupViews() {
        // Set welcome message
        binding.tvWelcome.text = "Hello ${AuthData.fullName}!"

        // Setup swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadDashboardData()
        }
    }

    private fun setupRecyclerView() {
        activitiesAdapter = RecentActivitiesAdapter { activity ->
            onActivityClicked(activity)
        }

        binding.rvRecentActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activitiesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavHelper = AdminBottomNavigationHelper(binding.bottomNavAdmin)

        bottomNavHelper.setOnTabSelectedListener { tab ->
            when (tab) {
                AdminBottomNavigationHelper.Tab.HOME -> {
                    // Already on home
                }
                AdminBottomNavigationHelper.Tab.POST_REQUESTS -> {
                    navigateToPostRequests()
                }
                AdminBottomNavigationHelper.Tab.PROFILE -> {
                    navigateToProfile()
                }
            }
        }

        // Set Home as selected
        bottomNavHelper.selectTab(AdminBottomNavigationHelper.Tab.HOME)
    }

    private fun setupListeners() {
        // Refresh button
        binding.btnRefresh.setOnClickListener {
            loadDashboardData()
        }

        // Statistics Cards Click Listeners
        binding.cardLostItems.setOnClickListener {
            navigateToLostItems()
        }

        binding.cardFoundItems.setOnClickListener {
            navigateToFoundItems()
        }

        binding.cardTotalClaims.setOnClickListener {
            navigateToAllClaims()
        }

        binding.cardPendingClaims.setOnClickListener {
            navigateToPendingClaims()
        }

        binding.cardApprovedClaims.setOnClickListener {
            navigateToApprovedClaims()
        }

        binding.cardTotalUsers.setOnClickListener {
            navigateToUsersList()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.adminDashboardState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading dashboard...")
                        binding.swipeRefresh.isRefreshing = true
                    }
                    is Resource.Success -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        val data = resource.data

                        // Update statistics
                        binding.tvLostItemsCount.text = data.totalLostItems.toString()
                        binding.tvFoundItemsCount.text = data.totalFoundItems.toString()
                        binding.tvTotalClaimsCount.text = data.totalClaims.toString()
                        binding.tvPendingClaimsCount.text = data.pendingClaims.toString()
                        binding.tvApprovedClaimsCount.text = data.approvedClaims.toString()
                        binding.tvTotalUsersCount.text = data.totalUsers.toString()

                        // Additional statistics
                        binding.tvVerifiedLostItems.text = "Verified: ${data.verifiedLostItems}"
                        binding.tvVerifiedFoundItems.text = "Verified: ${data.verifiedFoundItems}"
                        binding.tvReturnedItems.text = "Returned: ${data.returnedItems}"
                        binding.tvClaimedItems.text = "Claimed: ${data.claimedItems}"

                        // Update badge count for pending claims
                        bottomNavHelper.updateBadgeCount(data.pendingClaims)

                        // Update recent activities
                        activitiesAdapter.submitList(data.recentActivities)

                        // Show/hide empty state
                        if (data.recentActivities.isEmpty()) {
                            binding.tvNoActivities.visibility = View.VISIBLE
                            binding.rvRecentActivities.visibility = View.GONE
                        } else {
                            binding.tvNoActivities.visibility = View.GONE
                            binding.rvRecentActivities.visibility = View.VISIBLE
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        showError("Failed to load dashboard: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun loadDashboardData() {
        dashboardViewModel.getAdminDashboard()
    }

    private fun onActivityClicked(activity: ActivityItem) {
        when (activity.type) {
            "claim" -> {
                // Navigate to claim detail
                showInfo("Viewing claim: ${activity.title}")
                // findNavController().navigate(R.id.action_adminHomeFragment_to_claimDetailFragment,
                //     Bundle().apply { putString("claimId", activity.id) })
            }
            "found_item" -> {
                // Navigate to found item detail
                showInfo("Viewing found item: ${activity.title}")
                // findNavController().navigate(R.id.action_adminHomeFragment_to_itemDetailFragment,
                //     Bundle().apply { putString("itemId", activity.id) })
            }
            "lost_item" -> {
                // Navigate to lost item detail
                showInfo("Viewing lost item: ${activity.title}")
                // findNavController().navigate(R.id.action_adminHomeFragment_to_itemDetailFragment,
                //     Bundle().apply { putString("itemId", activity.id) })
            }
            "user_registration" -> {
                // Show user details or navigate to user profile
                showInfo("User registered: ${activity.title}")
            }
            else -> {
                showInfo("Activity: ${activity.title}")
            }
        }
    }

    // Navigation methods
    private fun navigateToPostRequests() {
        showInfo("Navigating to Post Requests")
         findNavController().navigate(R.id.action_adminHomeFragment_to_postRequestsFragment)
    }

    private fun navigateToProfile() {
        findNavController().navigate(R.id.action_adminHomeFragment_to_personalInfoFragment)
    }

    private fun navigateToLostItems() {
        showInfo("Navigate to Lost Items")
        // findNavController().navigate(R.id.action_adminHomeFragment_to_lostItemsListFragment)
    }

    private fun navigateToFoundItems() {
        showInfo("Navigate to Found Items")
        // findNavController().navigate(R.id.action_adminHomeFragment_to_foundItemsListFragment)
    }

    private fun navigateToAllClaims() {
        showInfo("Navigate to All Claims")
        // findNavController().navigate(R.id.action_adminHomeFragment_to_claimsListFragment)
    }

    private fun navigateToPendingClaims() {
        showInfo("Navigate to Pending Claims")
        // findNavController().navigate(R.id.action_adminHomeFragment_to_claimsListFragment,
        //     Bundle().apply { putString("filter", "pending") })
    }

    private fun navigateToApprovedClaims() {
        showInfo("Navigate to Approved Claims")
        // findNavController().navigate(R.id.action_adminHomeFragment_to_claimsListFragment,
        //     Bundle().apply { putString("filter", "approved") })
    }

    private fun navigateToUsersList() {
        showInfo("Navigate to Users List")
        // findNavController().navigate(R.id.action_adminHomeFragment_to_usersListFragment)
    }

    override fun onResume() {
        super.onResume()
        // Ensure Home tab is selected when returning to this fragment
        if (::bottomNavHelper.isInitialized) {
            bottomNavHelper.selectTab(AdminBottomNavigationHelper.Tab.HOME)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}