package com.example.lostandfound.feature.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentPersonalInfoBinding
import com.example.lostandfound.feature.auth.AuthViewModel
import com.example.lostandfound.feature.base.BaseFragment
import com.example.lostandfound.utils.AuthData
import org.koin.androidx.viewmodel.ext.android.viewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class PersonalInfoFragment : BaseFragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        AuthData.userDetailInfo?.let { user ->
            // Load profile image
            if (!user.profileImage.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .circleCrop()
                    .into(binding.ivProfilePhoto)
            }


            binding.etPhone.setText(user.phoneNumber)
            binding.etUserId.setText("${user.firstName} ${user.lastName}")
            binding.etEmail.setText( user.email)
        }
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }



    private fun logout() {
        authViewModel.logout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}