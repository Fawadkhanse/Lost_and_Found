package com.example.lostandfound.utils

import com.example.lostandfound.domain.auth.CurrentUserResponse
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

    fun setCurrentUserResponse(data: CurrentUserResponse) {
        fullName = data.firstName
        email = data.email
        phoneNumber = data.phoneNumber
        userDetailInfo .apply {
            this?.id = data.id
            this?.username = data.username
            email = data.email
            this?.firstName = data.firstName
            this?.lastName = data.lastName
            this?.userType = data.userType
            this?.phoneNumber = data.phoneNumber
            this?.towerNumber = data.towerNumber
            this?.roomNumber = data.roomNumber
            this?.profileImage = data.profileImage
        }
    }
}