package com.example.lostandfound.feature.auth

import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.ApiEndpoints
import com.example.lostandfound.data.HttpMethod
import com.example.lostandfound.data.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserLoginRequest(
    val email: String,
    val password: String,
    val userType: String = "customer"
)

data class UserLoginResponse(
    val responseCode: String,
    val responseMessage: String,
    val data: UserData?
)

data class UserData(
    val userId: String,
    val userName: String,
    val email: String,
    val token: String?,
    val userType: String
)

class AuthViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<Resource<UserLoginResponse>>(Resource.None)
    val loginState: StateFlow<Resource<UserLoginResponse>> = _loginState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // region API calls
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
                        _currentUser.value = result.data.data
                        _isLoggedIn.value = true
                    }
                },
                useMock = false
            )
        }
    }
    // endregion

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
        _loginState.value = Resource.None
    }
}
