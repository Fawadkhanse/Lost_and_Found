package com.example.lostandfound.feature.auth

import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.remote.ApiEndpoints
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.data.remote.TokenManager
import com.example.lostandfound.domain.auth.CurrentUserResponse
import com.example.lostandfound.domain.auth.LoginRequest
import com.example.lostandfound.domain.auth.LoginResponse
import com.example.lostandfound.domain.auth.RegisterRequest
import com.example.lostandfound.domain.auth.RegisterResponse
import com.example.lostandfound.domain.auth.UpdatePasswordResponse

import com.example.lostandfound.feature.base.BaseViewModel
import com.example.lostandfound.utils.AuthData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



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

    // Change Password State

    // Current User
    private val _currentUser = MutableStateFlow<CurrentUserResponse?>(null)
    val currentUser: StateFlow<CurrentUserResponse?> = _currentUser.asStateFlow()

    // Is Logged In
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // ===== API Calls =====

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
                        // Update user data and login status
                      //  _currentUser.value = result.data
                        _isLoggedIn.value = true

                        // Save token to TokenManager
                        result.data?.tokens?.let { token ->
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
     * Register new user
     */
    fun register(
       request: RegisterRequest
    ) {

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.REGISTER,
                httpMethod = HttpMethod.POST
            ).collectAsResource<RegisterResponse>(
                onEmit = { result ->
                    _registerState.value = result
                    if (result is Resource.Success) {
                        // Optionally auto-login after registration


                    //    _currentUser.value = result.data.data
                        _isLoggedIn.value = true

                    }
                },
                useMock = false
            )
        }
    }

//    /**
//     * Forgot password - send reset code to email
//     */
//    fun forgotPassword(email: String) {
//        val request = ForgotPasswordRequest(email)
//
//        viewModelScope.launch {
//            remoteRepository.makeApiRequest(
//                requestModel = request,
//                endpoint = ApiEndpoints.FORGOT_PASSWORD,
//                httpMethod = HttpMethod.POST
//            ).collectAsResource<ForgotPasswordResponse>(
//                onEmit = { result ->
//                    _forgotPasswordState.value = result
//                },
//                useMock = false
//            )
//        }
//    }
//
//    /**
//     * Reset password with code
//     */
//    fun resetPassword(email: String, code: String, newPassword: String) {
//        val request = ResetPasswordRequest(email, code, newPassword)
//
//        viewModelScope.launch {
//            remoteRepository.makeApiRequest(
//                requestModel = request,
//                endpoint = ApiEndpoints.RESET_PASSWORD,
//                httpMethod = HttpMethod.POST
//            ).collectAsResource<ResetPasswordResponse>(
//                onEmit = { result ->
//                    _resetPasswordState.value = result
//                },
//                useMock = false
//            )
//        }
//    }
//
//    /**
//     * Change password (requires authentication)
//     */
//    fun changePassword(oldPassword: String, newPassword: String) {
//        val request = ChangePasswordRequest(oldPassword, newPassword)
//
//        viewModelScope.launch {
//            remoteRepository.makeApiRequest(
//                requestModel = request,
//                endpoint = ApiEndpoints.CHANGE_PASSWORD,
//                httpMethod = HttpMethod.POST
//            ).collectAsResource<ChangePasswordResponse>(
//                onEmit = { result ->
//                    _changePasswordState.value = result
//                },
//                useMock = false
//            )
//        }
//    }

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