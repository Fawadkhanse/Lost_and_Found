package com.example.lostandfound

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.lostandfound.databinding.ActivityMainBinding
import com.example.lostandfound.utils.AdminBottomNavigationHelper
import com.example.lostandfound.utils.AuthData

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var adminBottomNavHelper: AdminBottomNavigationHelper? = null

    // Fragments that should show top bar
    private val topBarFragments = setOf(
        R.id.residentHomeFragment,
        R.id.adminHomeFragment,
        R.id.myListFragment,
        R.id.messagesFragment,
        R.id.personalInfoFragment,
        R.id.addItemFragment,
        R.id.itemDetailFragment,
        R.id.postRequestsFragment,
        R.id.claimFragment,
        R.id.chatFragment,
        R.id.reportLostItemFragment,
        R.id.categoryFragment,
        R.id.reportFoundItemFragment
    )

    // Fragments that should show resident bottom bar
    private val residentBottomBarFragments = setOf(
        R.id.residentHomeFragment,
        R.id.myListFragment,
        R.id.messagesFragment,
        R.id.personalInfoFragment
    )

    // Fragments that should show admin bottom bar
    private val adminBottomBarFragments = setOf(
        R.id.adminHomeFragment,
        R.id.postRequestsFragment,
        R.id.personalInfoFragment,
        R.id.categoryFragment
    )

    // Fragment titles
    private val fragmentTitles = mapOf(
        R.id.residentHomeFragment to "Lost & Found",
        R.id.adminHomeFragment to "Admin Dashboard",
        R.id.myListFragment to "My Lists",
        R.id.messagesFragment to "Messages",
        R.id.personalInfoFragment to "Profile",
        R.id.addItemFragment to "Report Item",
        R.id.itemDetailFragment to "Item Details",
        R.id.postRequestsFragment to "Post Requests",
        R.id.claimFragment to "Claim Item",
        R.id.chatFragment to "Chat",
        R.id.reportLostItemFragment to "Report Lost Item",
        R.id.reportFoundItemFragment to "Report Found Item",
        R.id.categoryFragment to "Add Categories"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupTopBar()
        setupBottomNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateUIForDestination(destination.id)
        }
    }
    fun refreshBottomBarUI() {
        val destinationId = navController.currentDestination?.id ?: return
        binding.root.postDelayed({
            updateUIForDestination(destinationId)
        }, 150) // short delay ensures destination + data are ready
    }

    private fun setupTopBar() {
        binding.topBar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.topBar.btnMenu.setOnClickListener {
            showMenuOptions()
        }
    }

    override fun onResume() {
        super.onResume()


    }

    private fun setupBottomNavigation() {
        setupResidentBottomNav()
        setupAdminBottomNav()
    }

    private fun setupResidentBottomNav() {
        binding.bottomNavResident.navHome.setOnClickListener {
            if (navController.currentDestination?.id != R.id.residentHomeFragment) {
                navController.navigate(R.id.residentHomeFragment)
            }
        }

        binding.bottomNavResident.navMessage.setOnClickListener {
            if (navController.currentDestination?.id != R.id.messagesFragment) {
                navController.navigate(R.id.messagesFragment)
            }
        }

        binding.bottomNavResident.navMyPost.setOnClickListener {
            if (navController.currentDestination?.id != R.id.myListFragment) {
                navController.navigate(R.id.myListFragment)
            }
        }

        binding.bottomNavResident.navAccount.setOnClickListener {
            if (navController.currentDestination?.id != R.id.personalInfoFragment) {
                navController.navigate(R.id.personalInfoFragment)
            }
        }
    }

    private fun setupAdminBottomNav() {
        adminBottomNavHelper = AdminBottomNavigationHelper(binding.bottomNavAdmin)

        adminBottomNavHelper?.setOnTabSelectedListener { tab ->
            when (tab) {
                AdminBottomNavigationHelper.Tab.HOME -> {
                    if (navController.currentDestination?.id != R.id.adminHomeFragment) {
                        navController.navigate(R.id.adminHomeFragment)
                    }
                }
                AdminBottomNavigationHelper.Tab.CATEGORIES -> {
                    if (navController.currentDestination?.id != R.id.categoryFragment) {
                        navController.navigate(R.id.categoryFragment)
                    }
                }
                AdminBottomNavigationHelper.Tab.POST_REQUESTS -> {
                    if (navController.currentDestination?.id != R.id.postRequestsFragment) {
                        navController.navigate(R.id.postRequestsFragment)
                    }
                }
                AdminBottomNavigationHelper.Tab.PROFILE -> {
                    if (navController.currentDestination?.id != R.id.personalInfoFragment) {
                        navController.navigate(R.id.personalInfoFragment)
                    }
                }
            }
        }
    }

    private fun updateUIForDestination(destinationId: Int) {
        // Update top bar
        if (destinationId in topBarFragments) {
            binding.topBar.root.visibility = View.VISIBLE
            binding.topBar.tvTopBarTitle.text = fragmentTitles[destinationId] ?: "Lost & Found"

            binding.topBar.btnBack.visibility = if (isRootFragment(destinationId)) {
                View.GONE
            } else {
                View.VISIBLE
            }

            binding.topBar.btnMenu.visibility = if (isRootFragment(destinationId)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        } else {
            binding.topBar.root.visibility = View.GONE
        }

        // Update bottom navigation
        val userType = AuthData.userDetailInfo?.userType

        when {
            destinationId in residentBottomBarFragments && userType == "resident" -> {
                showResidentBottomBar(destinationId)
            }
            destinationId in adminBottomBarFragments && userType == "admin" -> {
                showAdminBottomBar(destinationId)
            }
            else -> {
                hideAllBottomBars()
            }
        }
    }

    private fun isRootFragment(destinationId: Int): Boolean {
        return destinationId == R.id.residentHomeFragment ||
                destinationId == R.id.adminHomeFragment ||
                destinationId == R.id.loginFragment
    }

    private fun showResidentBottomBar(destinationId: Int) {
        binding.bottomNavResident.root.visibility = View.VISIBLE
        binding.bottomNavAdmin.root.visibility = View.GONE
        highlightResidentTab(destinationId)
    }

    private fun showAdminBottomBar(destinationId: Int) {
        binding.bottomNavResident.root.visibility = View.GONE
        binding.bottomNavAdmin.root.visibility = View.VISIBLE
        highlightAdminTab(destinationId)
    }

    private fun hideAllBottomBars() {
        binding.bottomNavResident.root.visibility = View.GONE
        binding.bottomNavAdmin.root.visibility = View.GONE
    }

    private fun highlightResidentTab(destinationId: Int) {
        resetResidentTabs()

        when (destinationId) {
            R.id.residentHomeFragment -> binding.bottomNavResident.navHome.alpha = 1.0f
            R.id.messagesFragment -> binding.bottomNavResident.navMessage.alpha = 1.0f
            R.id.myListFragment -> binding.bottomNavResident.navMyPost.alpha = 1.0f
            R.id.personalInfoFragment -> binding.bottomNavResident.navAccount.alpha = 1.0f
        }
    }

    private fun highlightAdminTab(destinationId: Int) {
        when (destinationId) {
            R.id.adminHomeFragment -> {
                adminBottomNavHelper?.selectTab(AdminBottomNavigationHelper.Tab.HOME)
            }
            R.id.postRequestsFragment -> {
                adminBottomNavHelper?.selectTab(AdminBottomNavigationHelper.Tab.POST_REQUESTS)
            }
            R.id.personalInfoFragment -> {
                adminBottomNavHelper?.selectTab(AdminBottomNavigationHelper.Tab.PROFILE)
            }
        }
    }

    private fun resetResidentTabs() {
        binding.bottomNavResident.navHome.alpha = 0.6f
        binding.bottomNavResident.navMessage.alpha = 0.6f
        binding.bottomNavResident.navMyPost.alpha = 0.6f
        binding.bottomNavResident.navAccount.alpha = 0.6f
    }

    private fun showMenuOptions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Menu")
            .setItems(arrayOf("Profile", "Logout")) { _, which ->
                when (which) {
                    0 -> navController.navigate(R.id.personalInfoFragment)
                    1 -> showLogoutConfirmation()
                }
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        AuthData.clearAuthData()
        navController.navigate(R.id.loginFragment)
    }

    fun updateMessageBadge(count: Int) {
        if (count > 0) {
            binding.bottomNavResident.badgeCount.visibility = View.VISIBLE
            binding.bottomNavResident.badgeCount.text = if (count > 99) "99+" else count.toString()
        } else {
            binding.bottomNavResident.badgeCount.visibility = View.GONE
        }
    }

    fun updateAdminBadge(count: Int) {
        adminBottomNavHelper?.updateBadgeCount(count)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}



