package com.example.lostandfound.feature.auth

import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.ApiEndpoints
import com.example.lostandfound.data.HttpMethod
import com.example.lostandfound.data.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.data.TokenManager
import com.example.lostandfound.domain.auth.ChangePasswordRequest
import com.example.lostandfound.domain.auth.ChangePasswordResponse
import com.example.lostandfound.domain.auth.ForgotPasswordRequest
import com.example.lostandfound.domain.auth.ForgotPasswordResponse
import com.example.lostandfound.domain.auth.ResetPasswordRequest
import com.example.lostandfound.domain.auth.ResetPasswordResponse
import com.example.lostandfound.domain.auth.UserData
import com.example.lostandfound.domain.auth.UserLoginRequest
import com.example.lostandfound.domain.auth.UserLoginResponse
import com.example.lostandfound.domain.auth.UserRegisterRequest
import com.example.lostandfound.domain.auth.UserRegisterResponse
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class AuthViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    // Login State
    private val _loginState = MutableStateFlow<Resource<UserLoginResponse>>(Resource.None)
    val loginState: StateFlow<Resource<UserLoginResponse>> = _loginState.asStateFlow()

    // Register State
    private val _registerState = MutableStateFlow<Resource<UserRegisterResponse>>(Resource.None)
    val registerState: StateFlow<Resource<UserRegisterResponse>> = _registerState.asStateFlow()

    // Forgot Password State
    private val _forgotPasswordState = MutableStateFlow<Resource<ForgotPasswordResponse>>(Resource.None)
    val forgotPasswordState: StateFlow<Resource<ForgotPasswordResponse>> = _forgotPasswordState.asStateFlow()

    // Reset Password State
    private val _resetPasswordState = MutableStateFlow<Resource<ResetPasswordResponse>>(Resource.None)
    val resetPasswordState: StateFlow<Resource<ResetPasswordResponse>> = _resetPasswordState.asStateFlow()

    // Change Password State
    private val _changePasswordState = MutableStateFlow<Resource<ChangePasswordResponse>>(Resource.None)
    val changePasswordState: StateFlow<Resource<ChangePasswordResponse>> = _changePasswordState.asStateFlow()

    // Current User
    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()

    // Is Logged In
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // ===== API Calls =====

    /**
     * Login user
     */
    fun login(email: String, password: String, userType: String = "customer") {
        val request = UserLoginRequest(email, password, userType)

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.LOGIN,
                httpMethod = HttpMethod.POST
            ).collectAsResource<UserLoginResponse>(
                onEmit = { result ->
                    _loginState.value = result
                    if (result is Resource.Success) {
                        // Update user data and login status
                        _currentUser.value = result.data.data
                        _isLoggedIn.value = true

                        // Save token to TokenManager
                        result.data.data?.token?.let { token ->
                            TokenManager.setToken(token)
                        }
                    }
                },
                useMock = false
            )
        }
    }

    /**
     * Register new user
     */
    fun register(
        email: String,
        password: String,
        userName: String,
        phone: String? = null,
        userType: String = "customer"
    ) {
        val request = UserRegisterRequest(email, password, userName, phone, userType)

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.REGISTER,
                httpMethod = HttpMethod.POST
            ).collectAsResource<UserRegisterResponse>(
                onEmit = { result ->
                    _registerState.value = result
                    if (result is Resource.Success) {
                        // Optionally auto-login after registration
                        _currentUser.value = result.data.data
                        _isLoggedIn.value = true

                        result.data.data?.token?.let { token ->
                            TokenManager.setToken(token)
                        }
                    }
                },
                useMock = false
            )
        }
    }

    /**
     * Forgot password - send reset code to email
     */
    fun forgotPassword(email: String) {
        val request = ForgotPasswordRequest(email)

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.FORGOT_PASSWORD,
                httpMethod = HttpMethod.POST
            ).collectAsResource<ForgotPasswordResponse>(
                onEmit = { result ->
                    _forgotPasswordState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Reset password with code
     */
    fun resetPassword(email: String, code: String, newPassword: String) {
        val request = ResetPasswordRequest(email, code, newPassword)

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.RESET_PASSWORD,
                httpMethod = HttpMethod.POST
            ).collectAsResource<ResetPasswordResponse>(
                onEmit = { result ->
                    _resetPasswordState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Change password (requires authentication)
     */
    fun changePassword(oldPassword: String, newPassword: String) {
        val request = ChangePasswordRequest(oldPassword, newPassword)

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.CHANGE_PASSWORD,
                httpMethod = HttpMethod.POST
            ).collectAsResource<ChangePasswordResponse>(
                onEmit = { result ->
                    _changePasswordState.value = result
                },
                useMock = false
            )
        }
    }

    // ===== User Management =====

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
     * Update current user data
     */
    fun updateUserData(userData: UserData) {
        _currentUser.value = userData
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

    /**
     * Reset change password state
     */
    fun resetChangePasswordState() {
        _changePasswordState.value = Resource.None
    }
}