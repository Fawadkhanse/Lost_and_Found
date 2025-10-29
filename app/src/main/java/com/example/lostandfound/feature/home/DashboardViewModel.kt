package com.example.lostandfound.feature.home


import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.ApiEndpoints
import com.example.lostandfound.data.HttpMethod
import com.example.lostandfound.data.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.domain.auth.AdminDashboardResponse
import com.example.lostandfound.domain.auth.UserDashboardResponse
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Dashboard
 */
class DashboardViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    // Admin Dashboard State
    private val _adminDashboardState = MutableStateFlow<Resource<AdminDashboardResponse>>(Resource.None)
    val adminDashboardState: StateFlow<Resource<AdminDashboardResponse>> = _adminDashboardState.asStateFlow()

    // User Dashboard State
    private val _userDashboardState = MutableStateFlow<Resource<UserDashboardResponse>>(Resource.None)
    val userDashboardState: StateFlow<Resource<UserDashboardResponse>> = _userDashboardState.asStateFlow()

    /**
     * Get admin dashboard data
     */
    fun getAdminDashboard() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.ADMIN_DASHBOARD,
                httpMethod = HttpMethod.GET
            ).collectAsResource<AdminDashboardResponse>(
                onEmit = { result ->
                    _adminDashboardState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get user dashboard data
     */
    fun getUserDashboard() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.USER_DASHBOARD,
                httpMethod = HttpMethod.GET
            ).collectAsResource<UserDashboardResponse>(
                onEmit = { result ->
                    _userDashboardState.value = result
                },
                useMock = false
            )
        }
    }

    fun resetStates() {
        _adminDashboardState.value = Resource.None
        _userDashboardState.value = Resource.None
    }
}




