package com.example.lostandfound.feature.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lostandfound.databinding.FragmentViewItemBinding

class ViewItemFragment : Fragment() {

    private var _binding: FragmentViewItemBinding? = null
    private val binding get() = _binding!!

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
        loadItemData()
        setupListeners()
    }

    private fun loadItemData() {
        // Load item data from arguments or database
        // For now using sample data
        binding.tvItemName.text = "Tablet"
        binding.tvCategory.text = "Electronic"
        binding.tvDateLost.text = "10/06/2025"
        binding.tvLocation.text = "Parking Blok"
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.cardScanItem.setOnClickListener {
            // Open camera or image picker
            Toast.makeText(requireContext(), "Opening camera...", Toast.LENGTH_SHORT).show()
        }

        binding.btnFound.setOnClickListener {
            // Mark item as found
            Toast.makeText(requireContext(), "Item marked as found", Toast.LENGTH_SHORT).show()
        }

        binding.btnPendingClaims.setOnClickListener {
            // Navigate to pending claims
            Toast.makeText(requireContext(), "Viewing pending claims", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}