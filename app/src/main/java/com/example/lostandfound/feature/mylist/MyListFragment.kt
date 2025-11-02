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

class MyListFragment : BaseFragment() {

    private var _binding: FragmentMyListBinding? = null
    private val binding get() = _binding!!

    private val itemViewModel: ItemViewModel by viewModel()
    private val claimViewModel: ClaimViewModel by viewModel()

    private lateinit var myListAdapter: MyListAdapter
    private var currentTab = Tab.POSTS

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
        selectTab(Tab.POSTS)
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
                        showLoading("Loading lost items...")
                    }
                    is Resource.Success -> {
                        hideLoading()
                        if (currentTab == Tab.POSTS) {
                            val lostItems = resource.data.results.map { item ->
                                MyListItem(
                                    id = item.id,
                                    title = item.title,
                                    status = item.status.replaceFirstChar { it.uppercase() },
                                    imageUrl = item.itemImage,
                                    type = ItemType.LOST_POST,
                                    createdAt = item.createdAt
                                )
                            }

                            // Combine with found items
                            val allPosts = mutableListOf<MyListItem>()
                            allPosts.addAll(lostItems)

                            // Get found items from their state
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
                        // Already showing loading from lost items
                    }
                    is Resource.Success -> {
                        hideLoading()
                        // Trigger refresh of lost items to combine them
                        if (currentTab == Tab.POSTS) {
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

                                val foundItems = resource.data.results.map { item ->
                                    MyListItem(
                                        id = item.id,
                                        title = item.title,
                                        status = item.status.replaceFirstChar { it.uppercase() },
                                        imageUrl = item.imageUrl ?: item.itemImage,
                                        type = ItemType.FOUND_POST,
                                        createdAt = item.createdAt
                                    )
                                }

                                val allPosts = mutableListOf<MyListItem>()
                                allPosts.addAll(lostItems)
                                allPosts.addAll(foundItems)
                                allPosts.sortByDescending { it.createdAt }

                                updateList(allPosts)
                            }
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        // Error already handled in lost items observer
                    }
                    Resource.None -> {
                        hideLoading()
                    }
                }
            }
        }
    }

    private fun selectTab(tab: Tab) {
        currentTab = tab

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
        myListAdapter.submitList(items)

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
                // TODO: Navigate to claim detail fragment
                // val bundle = Bundle().apply {
                //     putString("claimId", item.id)
                // }
                // findNavController().navigate(R.id.action_myListFragment_to_claimDetailFragment, bundle)
            }
            ItemType.LOST_POST -> {
                // Navigate to lost item detail
                val bundle = Bundle().apply {
                    putString("itemId", item.id)
                }

            }
            ItemType.FOUND_POST -> {
                // Navigate to found item detail
                val bundle = Bundle().apply {
                    putString("itemId", item.id)

                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}