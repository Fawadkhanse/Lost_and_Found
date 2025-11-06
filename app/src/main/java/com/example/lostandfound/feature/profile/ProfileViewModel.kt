package com.example.lostandfound.feature.profile


import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.remote.ApiEndpoints
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.data.repo.RemoteRepositoryImpl
import com.example.lostandfound.domain.auth.CurrentUserResponse
import com.example.lostandfound.domain.auth.RegisterResponse
import com.example.lostandfound.domain.auth.UpdatePasswordRequest
import com.example.lostandfound.domain.auth.UpdatePasswordResponse
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
    // Update Profile State
    private val _updateProfileState = MutableStateFlow<Resource<RegisterResponse>>(Resource.None)
    val updateProfileState: StateFlow<Resource<RegisterResponse>> = _updateProfileState.asStateFlow()

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
     * Update user profile
     * PUT /api/profile/
     * Supports updating: fullName, email, phoneNumber, address, and profileImage
     */
    /**
     * Register new user with multipart image
     */
    fun updateProfile(
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        userType: String,
        phoneNumber: String,
        towerNumber: String,
        roomNumber: String,
        profileImageFile: File?
    ) {
        viewModelScope.launch {
            // Create form data parameters
            val params = mutableMapOf<String, RequestBody>()
            params["username"] = createTextPart(username)
            params["email"] = createTextPart(email)
//            params["password"] = createTextPart(password)
//            params["password2"] = createTextPart(password2)
            params["first_name"] = createTextPart(firstName)
            params["last_name"] = createTextPart(lastName)
            params["user_type"] = createTextPart(userType)
            params["phone_number"] = createTextPart(phoneNumber)
            params["tower_number"] = createTextPart(towerNumber)
            params["room_number"] = createTextPart(roomNumber)

            // Create image part
            val imagePart = profileImageFile?.let {
                val requestFile = it.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profile_image", it.name, requestFile)
            }

            // Make multipart request
            remoteRepository.makeMultipartRequest(

                params = params,
                image = imagePart,
                endpoint = ApiEndpoints.UPDATE_PROFILE,
                     httpMethod =   HttpMethod.PUT
            ).collectAsResource<RegisterResponse>(
                onEmit = { result ->
                    _updateProfileState.value = result
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
    private fun createTextPart(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
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
 fun resetUpdateProfileState() {
        _updateProfileState.value = Resource.None
    }

    fun resetAllStates() {
        resetCurrentUserState()
        resetUpdatePasswordState()
    }
}