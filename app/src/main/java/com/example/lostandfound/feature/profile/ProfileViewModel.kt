package com.example.lostandfound.feature.profile


import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.remote.ApiEndpoints
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.domain.auth.CurrentUserResponse
import com.example.lostandfound.domain.auth.UpdatePasswordRequest
import com.example.lostandfound.domain.auth.UpdatePasswordResponse
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Profile operations
 * Handles:
 * - Get current user profile
 * - Update password
 */
class ProfileViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    // ============================================
    // State Flows
    // ============================================

    // Current User Profile State
    private val _currentUserState = MutableStateFlow<Resource<CurrentUserResponse>>(Resource.None)
    val currentUserState: StateFlow<Resource<CurrentUserResponse>> = _currentUserState.asStateFlow()

    // Update Password State
    private val _updatePasswordState = MutableStateFlow<Resource<UpdatePasswordResponse>>(Resource.None)
    val updatePasswordState: StateFlow<Resource<UpdatePasswordResponse>> = _updatePasswordState.asStateFlow()

    // ============================================
    // API Methods
    // ============================================

    /**
     * Get current user profile
     * GET /api/profile/
     */
    fun getCurrentUserProfile() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.PROFILE,
                httpMethod = HttpMethod.GET
            ).collectAsResource<CurrentUserResponse>(
                onEmit = { result ->
                    _currentUserState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get current logged-in user with profile image
     * GET /api/me/
     */
    fun getCurrentUser() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.CURRENT_USER,
                httpMethod = HttpMethod.GET
            ).collectAsResource<CurrentUserResponse>(
                onEmit = { result ->
                    _currentUserState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Update password
     * PUT /api/profile/password/
     */
    fun updatePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val request = UpdatePasswordRequest(
            oldPassword = oldPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.UPDATE_PASSWORD,
                httpMethod = HttpMethod.PUT
            ).collectAsResource<UpdatePasswordResponse>(
                onEmit = { result ->
                    _updatePasswordState.value = result
                },
                useMock = false
            )
        }
    }

    // ============================================
    // Reset Methods
    // ============================================

    fun resetCurrentUserState() {
        _currentUserState.value = Resource.None
    }

    fun resetUpdatePasswordState() {
        _updatePasswordState.value = Resource.None
    }

    fun resetAllStates() {
        resetCurrentUserState()
        resetUpdatePasswordState()
    }
}