package com.example.lostandfound.feature.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.data.Resource
import com.example.lostandfound.databinding.FragmentViewItemBinding
import com.example.lostandfound.domain.auth.LostItemResponse
import com.example.lostandfound.feature.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ViewItemFragment : BaseFragment() {

    private var _binding: FragmentViewItemBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val claimViewModel: ClaimViewModel by viewModel()

    private var itemId: String? = null
    private var currentItem: LostItemResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getString("itemId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModels()
        loadItemData()
    }

    private fun loadItemData() {
        itemId?.let {
            itemViewModel.getLostItemById(it)
        } ?: run {
            showError("Item ID not found")
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModels() {
        // Observe lost item detail
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.lostItemDetailState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading("Loading item details...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        currentItem = resource.data
                        displayItemData(resource.data)
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError("Failed to load item: ${resource.exception.message}")
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun displayItemData(item: LostItemResponse) {
        binding.tvItemName.text = item.title
        binding.tvCategory.text = item.categoryName
        binding.tvDateLost.text = item.lostDate
        binding.tvLocation.text = item.lostLocation

        // Load image if available
        if (!item.itemImage.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(item.itemImage)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.ivItemImage)

            // Hide scan text when image is loaded
            binding.tvScanItem.visibility = View.GONE
            binding.ivCamera.visibility = View.GONE
        }

        // Update button visibility based on status
        when (item.status.lowercase()) {
            "found", "claimed", "returned" -> {
                binding.btnFound.isEnabled = false
                binding.btnFound.alpha = 0.5f
                binding.btnFound.text = item.status.uppercase()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.cardScanItem.setOnClickListener {
            // TODO: Implement image picker
            Toast.makeText(requireContext(), "Image upload coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnFound.setOnClickListener {
            markItemAsFound()
        }

        binding.btnPendingClaims.setOnClickListener {
            viewPendingClaims()
        }

        // Bottom navigation
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.navHome.setOnClickListener {
            findNavController().navigate(R.id.residentHomeFragment)
        }

        binding.bottomNav.navMessage.setOnClickListener {
            Toast.makeText(requireContext(), "Messages", Toast.LENGTH_SHORT).show()
        }

        binding.bottomNav.navAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Account", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markItemAsFound() {
        // TODO: Implement update item status API
        Toast.makeText(
            requireContext(),
            "Mark as found feature coming soon",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun viewPendingClaims() {
        itemId?.let { id ->
            // TODO: Navigate to claims list for this item
            Toast.makeText(
                requireContext(),
                "Viewing claims for item: $id",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(itemId: String): ViewItemFragment {
            return ViewItemFragment().apply {
                arguments = Bundle().apply {
                    putString("itemId", itemId)
                }
            }
        }
    }
}