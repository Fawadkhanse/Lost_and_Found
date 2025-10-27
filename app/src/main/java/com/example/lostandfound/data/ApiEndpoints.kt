package com.example.lostandfound.data

object ApiEndpoints {
    // Auth endpoints
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val FORGOT_PASSWORD = "auth/forgot-password"
    const val RESET_PASSWORD = "auth/reset-password"
    const val CHANGE_PASSWORD = "auth/change-password"

    // Item endpoints
    const val LOST_ITEMS = "items/lost"
    const val FOUND_ITEMS = "items/found"
    const val MY_ITEMS = "items/my-items"
    const val ITEM_DETAIL = "items/{id}"
    const val CREATE_ITEM = "items/create"
    const val UPDATE_ITEM = "items/{id}/update"
    const val DELETE_ITEM = "items/{id}/delete"

    // Claim endpoints
    const val CLAIMS = "claims"
    const val MY_CLAIMS = "claims/my-claims"
    const val CREATE_CLAIM = "claims/create"
    const val CLAIM_DETAIL = "claims/{id}"
    const val APPROVE_CLAIM = "claims/{id}/approve"
    const val REJECT_CLAIM = "claims/{id}/reject"

    // Chat/Message endpoints
    const val MESSAGES = "messages"
    const val CONVERSATION = "messages/{userId}"
    const val SEND_MESSAGE = "messages/send"

    // Profile endpoints
    const val PROFILE = "profile"
    const val UPDATE_PROFILE = "profile/update"
    const val UPLOAD_IMAGE = "profile/upload-image"

    // Category endpoints
    const val CATEGORIES = "categories"
}