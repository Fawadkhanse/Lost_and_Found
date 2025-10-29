package com.example.lostandfound.utils

import com.example.lostandfound.domain.auth.LoginResponse
import com.example.lostandfound.domain.auth.UserDetailInfo

object AuthData {
    var fullName: String = ""
    var email: String = ""
    var phoneNumber: String = ""
    var userDetailInfo: UserDetailInfo? = null

    fun setAuthResponse(response: LoginResponse) {
        fullName = response.user.firstName
        email = response.user.email
        phoneNumber = response.user.phoneNumber
        userDetailInfo = response.user

    }

    fun clearAuthData() {
        fullName = ""
        email = ""
        phoneNumber = ""
        userDetailInfo = null
    }
}