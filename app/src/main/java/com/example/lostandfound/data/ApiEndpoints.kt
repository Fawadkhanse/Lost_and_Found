package com.example.lostandfound.data


object ApiEndpoints {
    // Auth endpoints
    const val REGISTER = "api/auth/register/"
    const val LOGIN = "api/auth/login/"

    // Profile endpoints
    const val PROFILE = "profile/"
    const val UPDATE_PASSWORD = "profile/password/"
    const val CURRENT_USER = "me/"
    const val ALL_USERS = "users/"

    // Category endpoints
    const val CATEGORIES = "categories/"

    // Lost Item endpoints
    const val LOST_ITEMS = "lost-items/"
    const val LOST_ITEM_DETAIL = "lost-items/{id}/"

    // Found Item endpoints
    const val FOUND_ITEMS = "found-items/"
    const val FOUND_ITEM_DETAIL = "found-items/{id}/"

    // Claim endpoints
    const val CLAIMS = "claims/"
    const val CLAIM_DETAIL = "claims/{id}/"

    // Notification endpoints
    const val NOTIFICATIONS = "notifications/"
    const val NOTIFICATION_DETAIL = "notifications/{id}/"
    const val MARK_NOTIFICATION_READ = "notifications/{id}/mark-read/"

    // Dashboard endpoints
    const val ADMIN_DASHBOARD = "dashboard/admin/"
    const val USER_DASHBOARD = "dashboard/user/"

    // Search endpoints
    const val MANUAL_IMAGE_SEARCH = "search/manual-image/"

    // Admin Verification endpoints
    const val VERIFY_LOST_ITEM = "admin/verify/lost-item/{id}/"
    const val VERIFY_FOUND_ITEM = "admin/verify/found-item/{id}/"
}