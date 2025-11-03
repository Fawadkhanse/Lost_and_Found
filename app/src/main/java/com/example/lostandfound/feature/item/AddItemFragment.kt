package com.example.lostandfound.feature.item

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentAddItemBinding
import com.example.lostandfound.domain.auth.CategoryResponse
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.feature.category.CategoryViewModel
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * AddItemFragment - Complete implementation for adding Lost or Found items
 */
class AddItemFragment : BaseFragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()

    private var categories: List<CategoryResponse> = emptyList()

    private var itemType: ItemType = ItemType.LOST // Default to Lost

    enum class ItemType {
        LOST, FOUND
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()


    }

    private fun setupViews() {
        // Set welcome message
        binding.tvWelcome.text = "Hello ${AuthData.fullName}"

        // Back button
              // Item type buttons (Lost or Found)
        binding.btnLost.setOnClickListener {
            selectItemType(ItemType.LOST)
        }

        binding.btnFound.setOnClickListener {
            selectItemType(ItemType.FOUND)
        }



    }

    private fun selectItemType(type: ItemType) {
        itemType = type

        when (type) {
            ItemType.LOST -> {
                binding.btnLost.backgroundTintList =
                    androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.primary_teal)
                binding.btnLost.alpha = 1.0f
                binding.btnFound.backgroundTintList = null
                binding.btnFound.alpha = 0.6f

                // Navigate to ReportLostItemFragment
                findNavController().navigate(R.id.action_addItemFragment_to_reportLostItemFragment)
            }
            ItemType.FOUND -> {
                binding.btnFound.backgroundTintList =
                    androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.primary_teal)
                binding.btnFound.alpha = 1.0f
                binding.btnLost.backgroundTintList = null
                binding.btnLost.alpha = 0.6f

                   findNavController().navigate(R.id.action_addItemFragment_to_reportFoundItemFragment)
            }
        }
    }

    private fun loadCategories() {
        categoryViewModel.getAllCategories()
    }

    private fun observeViewModels() {
        // Observe categories
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.categoriesListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading categories...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        categories = resource.data.results
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}