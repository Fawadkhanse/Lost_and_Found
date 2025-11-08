package com.example.lostandfound.feature.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.DialogAddCategoryBinding
import com.example.lostandfound.databinding.FragmentCategoryBinding
import com.example.lostandfound.domain.auth.CategoryResponse
import com.example.lostandfound.feature.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Admin Categories Fragment
 * Allows admins to view, add, edit, and delete categories
 */
class CategoryFragment : BaseFragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModel()
    private lateinit var categoriesAdapter: AdminCategoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        loadCategories()
    }

    private fun setupRecyclerView() {
        categoriesAdapter = AdminCategoriesAdapter(
            onEditClick = { category ->
                showAddEditCategoryDialog(category)
            },
            onDeleteClick = { category ->
                showDeleteConfirmation(category)
            }
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoriesAdapter
        }
    }

    private fun setupListeners() {
        // FAB - Add new category
        binding.fabAddCategory.setOnClickListener {
            showAddEditCategoryDialog(null)
        }

        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadCategories()
        }
    }

    private fun observeViewModel() {
        // Observe categories list
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.categoriesListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading categories...")
                        binding.swipeRefresh.isRefreshing = true
                    }
                    is Resource.Success -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        val categories = resource.data.results

                        if (categories.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                            binding.rvCategories.visibility = View.GONE
                        } else {
                            binding.emptyState.visibility = View.GONE
                            binding.rvCategories.visibility = View.VISIBLE
                            categoriesAdapter.submitList(categories)
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                        showError("Failed to load categories: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }

        // Observe create category state
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.createCategoryState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Creating category...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        showSuccess("Category created successfully!")
                        categoryViewModel.resetCreateState()
                        loadCategories() // Refresh list
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to create category: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun loadCategories() {
        categoryViewModel.getAllCategories()
    }

    /**
     * Show dialog to add or edit category
     */
    private fun showAddEditCategoryDialog(category: CategoryResponse?) {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)

        // Set title based on mode
        val isEditMode = category != null
        dialogBinding.tvDialogTitle.text = if (isEditMode) "Edit Category" else "Add Category"

        // Pre-fill data if editing
        category?.let {
            dialogBinding.etCategoryName.setText(it.name)
            dialogBinding.etCategoryDescription.setText(it.description)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Cancel button
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Save button
        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etCategoryName.text.toString().trim()
            val description = dialogBinding.etCategoryDescription.text.toString().trim()

            // Validation
            if (name.isEmpty()) {
                dialogBinding.tilCategoryName.error = "Category name is required"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                dialogBinding.tilCategoryDescription.error = "Description is required"
                return@setOnClickListener
            }

            // Clear errors
            dialogBinding.tilCategoryName.error = null
            dialogBinding.tilCategoryDescription.error = null

            // Create or update category
            if (isEditMode) {
                // TODO: Implement update category API
                showInfo("Update category feature coming soon")
                dialog.dismiss()
            } else {
                categoryViewModel.createCategory(name, description)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    /**
     * Show delete confirmation dialog
     */
    private fun showDeleteConfirmation(category: CategoryResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete \"${category.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Delete category
     */
    private fun deleteCategory(category: CategoryResponse) {
        // TODO: Implement delete category API
        showInfo("Delete category feature coming soon")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}