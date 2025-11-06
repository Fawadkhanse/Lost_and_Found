package com.example.lostandfound.feature.home


import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.remote.ApiEndpoints
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.data.repo.RemoteRepositoryImpl
import com.example.lostandfound.domain.auth.AdminDashboardResponse
import com.example.lostandfound.domain.auth.ManualSearchRequest
import com.example.lostandfound.domain.auth.ManualSearchResponse
import com.example.lostandfound.domain.auth.UserDashboardResponse
import com.example.lostandfound.domain.item.FoundItemResponse
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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
    private var _imageSearchState = MutableStateFlow<Resource<ManualSearchResponse>>(Resource.None)
    val imageSearchState: StateFlow<Resource<ManualSearchResponse>> = _imageSearchState.asStateFlow()


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

    fun manualSearch(requestModel: ManualSearchRequest){
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = requestModel,
                endpoint = ApiEndpoints.MANUAL_IMAGE_SEARCH,
                httpMethod = HttpMethod.POST
            ).collectAsResource<ManualSearchResponse>(
                onEmit = { result ->
                    _imageSearchState.value = result
                },
                useMock = false
            )
        }
    }
    fun imageBaseSearch(
        itemImageFile: File?
    ) {
        viewModelScope.launch {
            val params = mutableMapOf<String, RequestBody>()
            // Create image part
            val imagePart = itemImageFile?.let {
                val requestFile = it.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("item_image", it.name, requestFile)
            }
            remoteRepository.makeMultipartRequest(
                params = params,
                image = imagePart,
                endpoint = ApiEndpoints.IMAGE_BASED_SEARCH
            ).collectAsResource<ManualSearchResponse>(
                onEmit = { result ->
                    _imageSearchState.value = result
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




