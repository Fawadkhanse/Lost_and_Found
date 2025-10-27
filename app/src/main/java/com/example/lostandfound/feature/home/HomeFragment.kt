package com.example.lostandfound.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lostandfound.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // Set welcome text with user ID
        binding.tvWelcome.text = "Hello CB23145 !"
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnFound.setOnClickListener {
            // Navigate to Found items list
        }

        binding.btnLost.setOnClickListener {
            // Navigate to Lost items list
        }

        // Bottom navigation
        binding.bottomNav.navHome.setOnClickListener {
            // Already on home
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