package com.example.lostandfound.feature.mylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentMyListBinding

class MyListFragment : Fragment() {

    private var _binding: FragmentMyListBinding? = null
    private val binding get() = _binding!!
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
        setupViews()
        setupListeners()
        selectTab(Tab.POSTS)
    }

    private fun setupViews() {
        // Setup RecyclerView
        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // adapter = MyListAdapter(itemsList) // Add your adapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnClaims.setOnClickListener {
            selectTab(Tab.CLAIMS)
        }

        binding.btnPosts.setOnClickListener {
            selectTab(Tab.POSTS)
        }

        // Bottom navigation
        binding.bottomNav.navHome.setOnClickListener {
            // Navigate to home
        }

        binding.bottomNav.navMessage.setOnClickListener {
            // Navigate to messages
        }

        binding.bottomNav.navAccount.setOnClickListener {
            // Navigate to account
        }
    }

    private fun selectTab(tab: Tab) {
        currentTab = tab

        when (tab) {
            Tab.CLAIMS -> {
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
            }
            Tab.POSTS -> {
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
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}