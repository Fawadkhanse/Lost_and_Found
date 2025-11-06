// app/src/main/java/com/example/lostandfound/feature/home/ResidentHomeFragment.kt
package com.example.lostandfound.feature.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentResidentBinding
import com.example.lostandfound.domain.auth.LostItemResponse
import com.example.lostandfound.domain.auth.ManualSearchRequest
import com.example.lostandfound.domain.item.FoundItemResponse
import com.example.lostandfound.feature.auth.AuthViewModel
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.category.CategoryViewModel
import com.example.lostandfound.feature.item.ItemViewModel
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream


/**
 * Resident Home Fragment - ADVANCED VERSION
 * Displays lost and found items with advanced filtering and search capabilities
 */
class ResidentHomeFragment : BaseFragment() {

    private var _binding: FragmentResidentBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()

    private lateinit var itemsAdapter: ItemsAdapter
    private lateinit var categoriesAdapter: CategoriesAdapter
    private val allItems = mutableListOf<ItemModel>()

    // Track loading states
    private var isLostItemsLoaded = false
    private var isFoundItemsLoaded = false
    private var hasLoadedOnce = false
    private var isSearchMode = false

    // Search filters
    private var selectedSearchType = "lost" // "lost" or "found"
    private var selectedCategory = "All" // "All" means all categories

    private var selectedImageFile: File? = null

    // Available categories
    private var categories: List<String> = emptyList()

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
        setupCategoriesRecyclerView()
        setupSearchFilters()
        setupListeners()
        observeViewModels()
        setView()
        if (categories.isEmpty()){
            loadCategories()
        }
        // First load: Call search method with "All" category
        if (!hasLoadedOnce) {
            performInitialSearch()
        }
    }

    override fun onResume() {
        super.onResume()
        // Commented out: Now using search method on first load
        // if (!isSearchMode) {
        //     loadData()
        // }
    }

    private fun performInitialSearch() {
        // Set category to "All" (pass "All" to backend)
        selectedCategory = "All"

        val request = ManualSearchRequest(
            searchQuery = "",  // Empty query to get all items
            searchType = selectedSearchType,  // Default is "lost"
            category = "All"  // Pass "All" for all categories
        )

        //showInfo("Loading all ${selectedSearchType} items...")
        dashboardViewModel.manualSearch(request)
        isSearchMode = false  // This is the default view, not a search
    }

    private fun loadCategories() {
        categoryViewModel.getAllCategories()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isSearchMode) {
                        exitSearchMode()
                    } else {
                        showExitConfirmation()
                    }
                }
            }
        )
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(requireContext())
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
        }
    }

    private fun setupCategoriesRecyclerView() {
        categoriesAdapter = CategoriesAdapter { category ->
            onCategoryClicked(category)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoriesAdapter
        }
    }

    private fun setupSearchFilters() {
        // Setup Category Spinner with "All" as first item
        val categoryList = mutableListOf("All")
        categoryList.addAll(categories)

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryList
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Set default selection to "All"
        binding.spinnerCategory.setSelection(0)

        // Set default states
        updateSearchTypeUI()
    }

    private fun setupListeners() {
        // Text search - using IME action
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performTextSearch()
                true
            } else {
                false
            }
        }

        // Text search icon click
        binding.textSearch.setOnClickListener {
            performTextSearch()
        }

        // Image search icon click
        binding.imageSearch.setOnClickListener {
            openImagePicker()
        }

        // Search type toggle buttons
        binding.chipLost.setOnClickListener {
            selectedSearchType = "lost"
            updateSearchTypeUI()
        }

        binding.chipFound.setOnClickListener {
            selectedSearchType = "found"
            updateSearchTypeUI()
        }

        // Apply filters button
        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }

        // Clear filters button
        binding.btnClearFilters.setOnClickListener {
            clearFilters()
        }

        // Load more button
        binding.tvLoadMore.setOnClickListener {
            if (isSearchMode) {
                exitSearchMode()
            } else {
                loadMoreItems()
            }
        }

        // FAB to add new item
        binding.fabAdd.setOnClickListener {
            navigateTo(R.id.action_residentHomeFragment_to_addItemFragment)
        }

        // Toggle filter panel
        binding.btnToggleFilters.setOnClickListener {
            toggleFilterPanel()
        }
    }

    private fun onCategoryClicked(category: String) {
        selectedCategory = category

        // Update spinner selection
        val categoryList = mutableListOf("All")
        categoryList.addAll(categories)
        val position = categoryList.indexOf(selectedCategory)
        if (position >= 0) {
            binding.spinnerCategory.setSelection(position)
        }

        applyFilters()
    }

    private fun updateSearchTypeUI() {
        if (selectedSearchType == "lost") {
            binding.chipLost.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_teal))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            binding.chipFound.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            }
        } else {
            binding.chipFound.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_teal))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            binding.chipLost.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            }
        }
    }

    private fun toggleFilterPanel() {
        if (binding.filterPanel.visibility == View.VISIBLE) {
            binding.filterPanel.visibility = View.GONE
            binding.btnToggleFilters.text = "Show Filters ▼"
        } else {
            binding.filterPanel.visibility = View.VISIBLE
            binding.btnToggleFilters.text = "Hide Filters ▲"
        }
    }

    private fun applyFilters() {
        val categoryPosition = binding.spinnerCategory.selectedItemPosition
        selectedCategory = if (categoryPosition == 0) "All" else categories[categoryPosition - 1]

        val query = binding.etSearch.text.toString().trim()
        performTextSearch()
    }

    private fun clearFilters() {
        selectedSearchType = "lost"
        selectedCategory = "All"
        binding.spinnerCategory.setSelection(0)
        updateSearchTypeUI()
        binding.etSearch.text.clear()
        exitSearchMode()
        showInfo("Filters cleared")
    }

    private fun performTextSearch() {
        val query = binding.etSearch.text.toString().trim()

        isSearchMode = query.isNotEmpty() // Only set search mode if there's a query

        val categoryPosition = binding.spinnerCategory.selectedItemPosition
        val categoryFilter = if (categoryPosition == 0) "All" else categories[categoryPosition - 1]

        val request = ManualSearchRequest(
            searchQuery = query,
            searchType = selectedSearchType,
            category = categoryFilter
        )

        if (query.isNotEmpty()) {
            showInfo("Searching ${selectedSearchType} items" +
                    if (categoryFilter != "All") " in $categoryFilter" else "")
            updateSearchModeUI()
        }

        dashboardViewModel.manualSearch(request)
    }

    private fun filterCurrentItems() {
        if (allItems.isEmpty()) {
            showInfo("No items to filter")
            return
        }

        val filteredItems = allItems.filter { item ->
            val typeMatches = if (selectedSearchType == "lost") !item.isFound else item.isFound
            val categoryMatches = selectedCategory == "All" ||
                    item.categoryName.equals(selectedCategory, ignoreCase = true)

            typeMatches && categoryMatches
        }

        itemsAdapter.submitList(filteredItems.toList())

        val typeText = if (selectedSearchType == "lost") "Lost" else "Found"
        val categoryText = if (selectedCategory != "All") " in $selectedCategory" else ""
        showInfo("Showing ${filteredItems.size} $typeText item(s)$categoryText")
    }

    private fun exitSearchMode() {
        isSearchMode = false
        binding.etSearch.text.clear()
        selectedCategory = "All"
        binding.spinnerCategory.setSelection(0)
        updateSearchModeUI()
        // Use search method instead of loadData()
        performInitialSearch()
    }

    private fun updateSearchModeUI() {
        if (isSearchMode) {
            binding.tvLoadMore.text = "Clear Search"
            binding.tvLoadMore.visibility = View.VISIBLE
        } else {
            binding.tvLoadMore.text = "load more"
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageFile = createFileFromUri(uri)
                selectedImageFile?.let { file ->
                    isSearchMode = true
                    updateSearchModeUI()
                    showInfo("Searching by image for ${selectedSearchType} items")
                    dashboardViewModel.imageBaseSearch(file)
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun createFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "search_image_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun observeViewModels() {
        // Commented out: No longer using separate lost/found item loading
        /*
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.lostItemsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (!hasLoadedOnce) showLoading("Loading lost items...")
                    }
                    is Resource.Success -> {
                        isLostItemsLoaded = true
                        allItems.removeAll { !it.isFound }
                        val lostItems = resource.data.results.map { it.toItemModel(false) }
                        allItems.addAll(lostItems)
                        if (isFoundItemsLoaded) {
                            updateRecyclerView()
                            hideLoading()
                            hasLoadedOnce = true
                        }
                    }
                    is Resource.Error -> {
                        isLostItemsLoaded = true
                        hideLoading()
                        showError("Failed to load lost items: ${resource.exception.message}")
                        if (isFoundItemsLoaded) updateRecyclerView()
                    }
                    Resource.None -> {}
                }
            }
        }
        */

        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.imageSearchState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Searching by image...")
                    is Resource.Success -> {
                        hideLoading()
                        allItems.clear()
                        val searchResults = resource.data.results.map { it.toItemModel(false) }
                        allItems.addAll(searchResults)
                        updateRecyclerView()
                        hasLoadedOnce = true
                        if (searchResults.isEmpty()) {
                            showInfo("No matching items found")
                        } else {
                            //  showInfo("Found ${searchResults.size} matching item(s)")
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Image search failed: ${resource.exception.message}")
                    }
                    Resource.None -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.imageSearchState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading("Searching...")
                    is Resource.Success -> {
                        hideLoading()
                        allItems.clear()
                        val searchResults = resource.data.results.map { it.toItemModel(false) }
                        allItems.addAll(searchResults)
                        updateRecyclerView()
                        hasLoadedOnce = true

                        val typeText = if (selectedSearchType == "lost") "Lost" else "Found"
                        val categoryText = if (selectedCategory != "All") " in $selectedCategory" else ""

                        if (searchResults.isEmpty()) {
                            showInfo("No matching $typeText items found$categoryText")
                        } else {
                            //  showInfo("Found ${searchResults.size} $typeText item(s)$categoryText")
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Search failed: ${resource.exception.message}")
                    }
                    Resource.None -> {}
                }
            }
        }

        // Commented out: No longer using separate found items loading
        /*
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.foundItemsListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (!hasLoadedOnce) showLoading("Loading found items...")
                    }
                    is Resource.Success -> {
                        isFoundItemsLoaded = true
                        allItems.removeAll { it.isFound }
                        val foundItems = resource.data.results.map { it.toItemModel(true) }
                        allItems.addAll(foundItems)
                        if (isLostItemsLoaded) {
                            updateRecyclerView()
                            hideLoading()
                            hasLoadedOnce = true
                        }
                    }
                    is Resource.Error -> {
                        isFoundItemsLoaded = true
                        hideLoading()
                        showError("Failed to load found items: ${resource.exception.message}")
                        if (isLostItemsLoaded) updateRecyclerView()
                    }
                    Resource.None -> {}
                }
            }
        }
        */

        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.categoriesListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading categories...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        categories = resource.data.results.map { it.name ?: "" }
                        setupSearchFilters()

                        // Update categories RecyclerView
                        val categoryList = mutableListOf("All")
                        categoryList.addAll(categories)
                        categoriesAdapter.submitList(categoryList)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load categories: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.userDashboardState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {}
                    is Resource.Error -> {}
                    else -> {}
                }
            }
        }
    }

    // Commented out: No longer using this method for initial load
    /*
    private fun loadData() {
        isLostItemsLoaded = false
        isFoundItemsLoaded = false
        itemViewModel.getAllLostItems()
        itemViewModel.getAllFoundItems()
    }
    */

    private fun loadMoreItems() {
        showInfo("Refreshing items...")
        performInitialSearch()
    }

    private fun updateRecyclerView() {
        val sortedItems = allItems.sortedByDescending { it.date }
        itemsAdapter.submitList(sortedItems.toList())

        if (sortedItems.isEmpty()) {
            binding.rvPosts.visibility = View.GONE
            if (!isSearchMode) {
                binding.tvLoadMore.visibility = View.GONE
            }
        } else {
            binding.rvPosts.visibility = View.VISIBLE
            binding.tvLoadMore.visibility = View.VISIBLE
        }
    }

    private fun onItemClicked(item: ItemModel) {
        val bundle = Bundle().apply {
            putString("itemId", item.id)
            putString("itemType", if (item.isFound) "FOUND" else "LOST")
        }
        navigateTo(R.id.action_residentHomeFragment_to_itemDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun LostItemResponse.toItemModel(isFound: Boolean = false): ItemModel {
        return ItemModel(
            id = this.id ?: "",
            title = this.title ?: "Unknown",
            categoryName = this.categoryName ?: "",
            date = this.lostDate ?: "",
            location = this.lostLocation ?: "",
            imageUrl = this.itemImage ?: "",
            isFound = isFound,
            status = this.status ?: ""
        )
    }

    private fun FoundItemResponse.toItemModel(isFound: Boolean = true): ItemModel {
        return ItemModel(
            id = this.id ?: "",
            title = this.title ?: "Unknown",
            categoryName = this.categoryName ?: "",
            date = this.foundDate ?: "",
            location = this.foundLocation ?: "",
            imageUrl = this.imageUrl ?: this.itemImage ?: "",
            isFound = isFound,
            status = this.status ?: ""
        )
    }
}