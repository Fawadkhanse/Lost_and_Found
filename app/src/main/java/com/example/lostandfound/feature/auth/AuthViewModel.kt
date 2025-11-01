// app/src/main/java/com/example/lostandfound/feature/auth/AuthViewModel.kt
package com.example.lostandfound.feature.auth

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.remote.ApiEndpoints
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.data.remote.TokenManager
import com.example.lostandfound.data.repo.RemoteRepositoryImpl
import com.example.lostandfound.domain.auth.*
import com.example.lostandfound.feature.base.BaseViewModel
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AuthViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    // Login State
    private val _loginState = MutableStateFlow<Resource<LoginResponse>>(Resource.None)
    val loginState: StateFlow<Resource<LoginResponse>> = _loginState.asStateFlow()

    // Register State
    private val _registerState = MutableStateFlow<Resource<RegisterResponse>>(Resource.None)
    val registerState: StateFlow<Resource<RegisterResponse>> = _registerState.asStateFlow()

    // Forgot Password State
    private val _forgotPasswordState = MutableStateFlow<Resource<UpdatePasswordResponse>>(Resource.None)
    val forgotPasswordState: StateFlow<Resource<UpdatePasswordResponse>> = _forgotPasswordState.asStateFlow()

    // Reset Password State
    private val _resetPasswordState = MutableStateFlow<Resource<UpdatePasswordResponse>>(Resource.None)
    val resetPasswordState: StateFlow<Resource<UpdatePasswordResponse>> = _resetPasswordState.asStateFlow()

    // Current User
    private val _currentUser = MutableStateFlow<CurrentUserResponse?>(null)
    val currentUser: StateFlow<CurrentUserResponse?> = _currentUser.asStateFlow()

    // Is Logged In
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * Login user
     */
    fun login(email: String, password: String) {
        val request = LoginRequest(email, password)

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.LOGIN,
                httpMethod = HttpMethod.POST
            ).collectAsResource<LoginResponse>(
                onEmit = { result ->
                    _loginState.value = result
                    if (result is Resource.Success) {
                        _isLoggedIn.value = true
                        result.data.tokens?.let { token ->
                            TokenManager.setToken(token.access)
                        }
                        AuthData.setAuthResponse(result.data)
                    }
                },
                useMock = false
            )
        }
    }

    /**
     * Register new user with multipart image
     */
    fun registerWithImage(
        username: String,
        email: String,
        password: String,
        password2: String,
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
            params["password"] = createTextPart(password)
            params["password2"] = createTextPart(password2)
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
            (remoteRepository as RemoteRepositoryImpl).makeMultipartRequest(
                params = params,
                image = imagePart,
                endpoint = ApiEndpoints.REGISTER
            ).collectAsResource<RegisterResponse>(
                onEmit = { result ->
                    _registerState.value = result
                    if (result is Resource.Success) {
                        _isLoggedIn.value = true
                    }
                },
                useMock = false
            )
        }
    }

    private fun createTextPart(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /**
     * Logout user
     */
    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
        _loginState.value = Resource.None
        _registerState.value = Resource.None
        TokenManager.clearToken()
    }

    /**
     * Check if user is logged in
     */
    fun checkLoginStatus(): Boolean {
        return _isLoggedIn.value && TokenManager.getToken() != null
    }

    /**
     * Reset login state
     */
    fun resetLoginState() {
        _loginState.value = Resource.None
    }

    /**
     * Reset register state
     */
    fun resetRegisterState() {
        _registerState.value = Resource.None
    }

    /**
     * Reset forgot password state
     */
    fun resetForgotPasswordState() {
        _forgotPasswordState.value = Resource.None
    }

    /**
     * Reset reset password state
     */
    fun resetResetPasswordState() {
        _resetPasswordState.value = Resource.None
    }
}