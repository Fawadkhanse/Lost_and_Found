package com.example.lostandfound.domain.auth

import android.R
import com.example.lostandfound.domain.item.FoundItemResponse
import com.google.gson.annotations.SerializedName

// ============================================
// Authentication Data Classes
// ============================================

data class RegisterRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("password2")
    val password2: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("user_type")
    val userType: String, // "admin" or "resident"

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("tower_number")
    val towerNumber: String,

    @SerializedName("room_number")
    val roomNumber: String,

    @SerializedName("profile_image")
    val profileImage: String? = null
)

data class RegisterResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: UserBasicInfo,

    @SerializedName("refresh")
    val refresh: String,

    @SerializedName("access")
    val access: String,

    @SerializedName("redirect_url")
    val redirectUrl: String
)

data class UserBasicInfo(
    @SerializedName("id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("user_type")
    val userType: String,

    @SerializedName("is_staff")
    val isStaff: Boolean,

    @SerializedName("is_superuser")
    val isSuperuser: Boolean
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: UserDetailInfo,

    @SerializedName("tokens")
    val tokens: TokenResponse,

    @SerializedName("redirect_url")
    val redirectUrl: String
)

data class UserDetailInfo(
    @SerializedName("id")
    var id: String,

    @SerializedName("username")
    var username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("first_name")
    var firstName: String,

    @SerializedName("last_name")
    var lastName: String,

    @SerializedName("user_type")
    var userType: String,

    @SerializedName("phone_number")
    var phoneNumber: String,

    @SerializedName("tower_number")
    var towerNumber: String,

    @SerializedName("room_number")
    var roomNumber: String,

    @SerializedName("profile_image")
    var profileImage: String?,

    @SerializedName("is_staff")
    val isStaff: Boolean,

    @SerializedName("is_superuser")
    val isSuperuser: Boolean
)

data class TokenResponse(
    @SerializedName("refresh")
    val refresh: String,

    @SerializedName("access")
    val access: String
)

// ============================================
// Profile Data Classes
// ============================================

data class ProfileResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("user_type")
    val userType: String,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("tower_number")
    val towerNumber: String,

    @SerializedName("room_number")
    val roomNumber: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class UpdatePasswordRequest(
    @SerializedName("old_password")
    val oldPassword: String,

    @SerializedName("new_password")
    val newPassword: String,

    @SerializedName("confirm_password")
    val confirmPassword: String
)

data class UpdatePasswordResponse(
    @SerializedName("detail")
    val detail: String
)

data class CurrentUserResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("user_type")
    val userType: String,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("tower_number")
    val towerNumber: String,

    @SerializedName("room_number")
    val roomNumber: String,

    @SerializedName("profile_image")
    val profileImage: String?,

    @SerializedName("profile_image_url")
    val profileImageUrl: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

// ============================================
// User Management Data Classes
// ============================================

data class AllUsersResponse(
    @SerializedName("count")
    val count: Int,

    @SerializedName("users")
    val users: List<UserItem>
)

data class UserItem(
    @SerializedName("id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("user_type")
    val userType: String,

    @SerializedName("phone_number")
    val phoneNumber: String?,

    @SerializedName("tower_number")
    val towerNumber: String?,

    @SerializedName("room_number")
    val roomNumber: String?,

    @SerializedName("profile_image")
    val profileImage: String?,

    @SerializedName("date_joined")
    val dateJoined: String,

    @SerializedName("is_active")
    val isActive: Boolean
)

// ============================================
// Category Data Classes
// ============================================

data class CategoryRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String
)

data class CategoryListResponse(
    @SerializedName("count")
    val count: Int?,

    @SerializedName("next")
    val next: String? = null,

    @SerializedName("previous")
    val previous: String? = null,

    @SerializedName("results")
    val results: List<CategoryResponse>
)

data class CategoryResponse(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)

// ============================================
// Lost Item Data Classes
// ============================================

data class LostItemRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: Int,

    @SerializedName("lost_location")
    val lostLocation: String,

    @SerializedName("lost_date")
    val lostDate: String,

    @SerializedName("lost_time")
    val lostTime: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("color")
    val color: String,

    @SerializedName("size")
    val size: String,

    @SerializedName("search_tags")
    val searchTags: String,

    @SerializedName("color_tags")
    val colorTags: String,

    @SerializedName("material_tags")
    val materialTags: String,

    @SerializedName("status")
    val status: String = "lost",

    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    @SerializedName("item_image")
    val itemImage: String?
)

data class LostItemsListResponse(
    @SerializedName("count")
    val count: Int?,

    @SerializedName("next")
    val next: String?,

    @SerializedName("previous")
    val previous: String?,

    @SerializedName("results")
    val results: List<LostItemResponse>
)
data class MyItemsListResponse(
    @SerializedName("lost_items")
    val myLost: List<LostItemResponse>,
    @SerializedName("found_items")
    val myFound: List<FoundItemResponse>


)
data class LostItemResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("user")
    val user: String,
    @SerializedName("user_id")

    val userId: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: Int,

    @SerializedName("category_name")
    val categoryName: String,

    @SerializedName("search_tags")
    val searchTags: String,

    @SerializedName("color_tags")
    val colorTags: String,

    @SerializedName("material_tags")
    val materialTags: String,

    // This can be null if the item is found, not lost
    @SerializedName("lost_location")
    val lostLocation: String?=null,
    @SerializedName("found_time")
    val foundTime: String?=null,

    @SerializedName("storage_location")
    val storageLocation: String?=null,



    // This can be null if the item is found, not lost
    @SerializedName("lost_date")
    val lostDate: String?=null,

    // This will be null for lost items
    @SerializedName("found_date")
    val foundDate: String?=null,

    // This will be null for lost items
    @SerializedName("found_location")
    val foundLocation: String?=null,

    // This can be null if the item is found, not lost
    @SerializedName("lost_time")
    val lostTime: String?,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("color")
    val color: String,

    @SerializedName("size")
    val size: String,

    @SerializedName("item_image")
    val itemImage: String?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("is_verified")
    val isVerified: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("search_tags_list")
    val searchTagsList: List<String>,

    @SerializedName("color_tags_list")
    val colorTagsList: List<String>,

    @SerializedName("material_tags_list")
    val materialTagsList: List<String>
): java.io.Serializable

// ============================================
// Found Item Data Classes
// ============================================



// ============================================
// Claim Data Classes
// ============================================

data class ClaimRequest(
    @SerializedName("found_item")
    val foundItem: String,

    @SerializedName("claim_description")
    val claimDescription: String,

    @SerializedName("proof_of_ownership")
    val proofOfOwnership: String,

    @SerializedName("supporting_images")
    val supportingImages: String?,

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("admin_notes")
    val adminNotes: String?
)

data class ClaimsListResponse(
    @SerializedName("count")
    val count: Int,

    @SerializedName("next")
    val next: String?,

    @SerializedName("previous")
    val previous: String?,

    @SerializedName("results")
    val results: List<ClaimResponse>
)

data class ClaimResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("user")
    val user: String,

    @SerializedName("user_email")
    val userEmail: String,

    @SerializedName("found_item")
    val foundItem: String,

    @SerializedName("found_item_title")
    val foundItemTitle: String,

    @SerializedName("found_item_image")
    val foundItemImage: String?,

    @SerializedName("claim_description")
    val claimDescription: String,

    @SerializedName("proof_of_ownership")
    val proofOfOwnership: String,

    @SerializedName("supporting_images")
    val supportingImages: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("admin_notes")
    val adminNotes: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("resolved_at")
    val resolvedAt: String?
)

// ============================================
// Notification Data Classes
// ============================================

data class NotificationRequest(
    @SerializedName("notification_type")
    val notificationType: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("lost_item")
    val lostItem: String?
)

data class NotificationsListResponse(
    @SerializedName("count")
    val count: Int,

    @SerializedName("results")
    val results: List<NotificationResponse>
)

data class NotificationResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("user")
    val user: String,

    @SerializedName("notification_type")
    val notificationType: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("lost_item")
    val lostItem: String?,

    @SerializedName("found_item")
    val foundItem: String?,

    @SerializedName("claim")
    val claim: String?,

    @SerializedName("is_read")
    val isRead: Boolean,

    @SerializedName("created_at")
    val createdAt: String
)

// ============================================
// Dashboard Data Classes
// ============================================

data class AdminDashboardResponse(
    @SerializedName("total_lost_items")
    val totalLostItems: Int,

    @SerializedName("total_found_items")
    val totalFoundItems: Int,

    @SerializedName("total_claims")
    val totalClaims: Int,

    @SerializedName("pending_claims")
    val pendingClaims: Int,

    @SerializedName("approved_claims")
    val approvedClaims: Int,

    @SerializedName("total_users")
    val totalUsers: Int,

    @SerializedName("recent_activities")
    val recentActivities: List<ActivityItem>,

    @SerializedName("verified_lost_items")
    val verifiedLostItems: Int,

    @SerializedName("verified_found_items")
    val verifiedFoundItems: Int,

    @SerializedName("returned_items")
    val returnedItems: Int,

    @SerializedName("claimed_items")
    val claimedItems: Int,

    @SerializedName("user_registrations_today")
    val userRegistrationsToday: Int
)

data class ActivityItem(
    @SerializedName("type")
    val type: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("user")
    val user: String? = null,

    @SerializedName("user_type")
    val userType: String? = null,

    @SerializedName("date")
    val date: String,

    @SerializedName("id")
    val id: String
)

data class UserDashboardResponse(
    @SerializedName("total_lost_items")
    val totalLostItems: Int,

    @SerializedName("total_found_items")
    val totalFoundItems: Int,

    @SerializedName("total_claims")
    val totalClaims: Int,

    @SerializedName("pending_claims")
    val pendingClaims: Int,

    @SerializedName("approved_claims")
    val approvedClaims: Int,

    @SerializedName("recent_activities")
    val recentActivities: List<ActivityItem>
)

// ============================================
// Search Data Classes
// ============================================

data class ManualSearchRequest(
    @SerializedName("search_query")
    val searchQuery: String,

    @SerializedName("search_type")
    val searchType: String="lost",

    @SerializedName("category_filters")
    val category: String = ""
)

data class ManualSearchResponse(
    @SerializedName("search_metadata")
    val searchMetadata: SearchMetadata,

    @SerializedName("results")
    val results: List<LostItemResponse> // Using Any as the results can be empty
)

data class SearchMetadata(
    @SerializedName("query")
    val query: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("filters_applied")
    val filtersApplied: FiltersApplied,

    @SerializedName("category_details")
    val categoryDetails: CategoryDetails?,

    @SerializedName("category_exists")
    val categoryExists: Boolean,

    @SerializedName("results_count")
    val resultsCount: Int,

    @SerializedName("search_duration_seconds")
    val searchDurationSeconds: Double,

    @SerializedName("max_results")
    val maxResults: Int
)

data class FiltersApplied(
    @SerializedName("colors")
    val colors: String,

    @SerializedName("categories")
    val categories: String
)

data class CategoryDetails(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?
)



/**
 * Request model for reset password (when user has reset token)
 */
data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("new_password")
    val newPassword: String,

    @SerializedName("confirm_password")
    val confirmPassword: String
)

/**
 * Response model for reset password
 */
data class ResetPasswordResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("detail")
    val detail: String? = null
)




