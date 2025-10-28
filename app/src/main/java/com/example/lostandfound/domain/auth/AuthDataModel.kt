package com.example.lostandfound.domain.auth

// Request Models
data class UserLoginRequest(
    val email: String,
    val password: String,
    val userType: String = "customer"
)

data class UserRegisterRequest(
    val email: String,
    val password: String,
    val userName: String,
    val phone: String? = null,
    val userType: String = "customer"
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

// Response Models
data class UserLoginResponse(
    val responseCode: String,
    val responseMessage: String,
    val data: UserData?
)

data class UserRegisterResponse(
    val responseCode: String,
    val responseMessage: String,
    val data: UserData?
)

data class ForgotPasswordResponse(
    val responseCode: String,
    val responseMessage: String
)

data class ResetPasswordResponse(
    val responseCode: String,
    val responseMessage: String
)

data class ChangePasswordResponse(
    val responseCode: String,
    val responseMessage: String
)

data class UserData(
    val userId: String,
    val userName: String,
    val email: String,
    val token: String?,
    val userType: String,
    val phone: String? = null,
    val faculty: String? = null,
    val address: String? = null,
    val profileImage: String? = null
)