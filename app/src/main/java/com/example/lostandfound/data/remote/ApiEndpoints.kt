package com.example.lostandfound.data.remote


object ApiEndpoints {
    // Auth endpoints
    const val REGISTER = "api/auth/register/"
    const val LOGIN = "api/auth/login/"

    // Profile endpoints
    const val PROFILE = "profile/"
    const val UPDATE_PROFILE = "api/profile/me/"
    const val UPDATE_PASSWORD = "api/profile/password/"
    const val FORGOT_PASSWORD = "api/profile/forgot_password/"

    const val CURRENT_USER = "api/me/"
    const val ALL_USERS = "users/"

    // Category endpoints
    const val CATEGORIES = "categories/"

    // Lost Item endpoints
    const val LOST_ITEMS = "lost-items/"
    const val  MY_ITEMS = "my-items/"
    const val LOST_ITEM_DETAIL = "lost-items/{id}/"
    const val LOST_ITEM_DELETE = "lost-items/{id}/"

    // Found Item endpoints
    const val FOUND_ITEMS = "found-items/"
    const val FOUND_ITEM_DETAIL = "found-items/{id}/"
    const val FOUND_ITEM_DELETE = "found-items/{id}/"

    // Claim endpoints
    const val CLAIMS = "claims/"
    const val CLAIM_DETAIL = "claims/{id}/"

    // Notification endpoints
    const val NOTIFICATIONS = "notifications/"
    const val NOTIFICATION_DETAIL = "notifications/{id}/"
    const val MARK_NOTIFICATION_READ = "api/notifications/{id}/mark-read/"

    // Dashboard endpoints
    const val ADMIN_DASHBOARD = "dashboard/admin/"
    const val USER_DASHBOARD = "dashboard/user/"

    // Search endpoints
    const val MANUAL_IMAGE_SEARCH = "search/manual-image/"
    const val IMAGE_BASED_SEARCH = "image-based-search/"

    // Admin Verification endpoints
    const val VERIFY_LOST_ITEM = "admin/verify/lost-item/{id}/"
    const val VERIFY_FOUND_ITEM = "admin/verify/found-item/{id}/"

}