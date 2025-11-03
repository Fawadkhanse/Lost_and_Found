package com.example.lostandfound.domain.item

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class FoundItemRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: Int,

    @SerializedName("found_location")
    val foundLocation: String,

    @SerializedName("found_date")
    val foundDate: String,

    @SerializedName("found_time")
    val foundTime: String,

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

    @SerializedName("storage_location")
    val storageLocation: String,

    @SerializedName("status")
    val status: String = "found",

    @SerializedName("image_url")
    val imageUrl: String?
)

data class FoundItemsListResponse(
    @SerializedName("count")
    val count: Int,

    @SerializedName("next")
    val next: String?,

    @SerializedName("previous")
    val previous: String?,

    @SerializedName("results")
    val results: List<FoundItemResponse>
)

data class FoundItemResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("user")
    val user: String,

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

    @SerializedName("found_location")
    val foundLocation: String,

    @SerializedName("found_date")
    val foundDate: String,

    @SerializedName("found_time")
    val foundTime: String,

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

    @SerializedName("storage_location")
    val storageLocation: String,

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