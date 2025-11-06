// app/src/main/java/com/example/lostandfound/feature/item/ItemViewModel.kt
package com.example.lostandfound.feature.item

import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.remote.ApiEndpoints
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.data.repo.RemoteRepositoryImpl
import com.example.lostandfound.domain.VerificationResponse
import com.example.lostandfound.domain.auth.LostItemRequest
import com.example.lostandfound.domain.auth.LostItemResponse
import com.example.lostandfound.domain.auth.LostItemsListResponse
import com.example.lostandfound.domain.item.FoundItemRequest
import com.example.lostandfound.domain.item.FoundItemResponse
import com.example.lostandfound.domain.item.FoundItemsListResponse
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ItemViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    // ============================================
    // Lost Items State Flows
    // ============================================

    private val _createLostItemState = MutableStateFlow<Resource<LostItemResponse>>(Resource.None)
    val createLostItemState: StateFlow<Resource<LostItemResponse>> = _createLostItemState.asStateFlow()

    private val _lostItemsListState = MutableStateFlow<Resource<LostItemsListResponse>>(Resource.None)
    val lostItemsListState: StateFlow<Resource<LostItemsListResponse>> = _lostItemsListState.asStateFlow()

    private val _lostItemDetailState = MutableStateFlow<Resource<LostItemResponse>>(Resource.None)
    val lostItemDetailState: StateFlow<Resource<LostItemResponse>> = _lostItemDetailState.asStateFlow()

    // ============================================
    // Found Items State Flows
    // ============================================

    private val _createFoundItemState = MutableStateFlow<Resource<FoundItemResponse>>(Resource.None)
    val createFoundItemState: StateFlow<Resource<FoundItemResponse>> = _createFoundItemState.asStateFlow()

    private val _foundItemsListState = MutableStateFlow<Resource<FoundItemsListResponse>>(Resource.None)
    val foundItemsListState: StateFlow<Resource<FoundItemsListResponse>> = _foundItemsListState.asStateFlow()

    private val _foundItemDetailState = MutableStateFlow<Resource<FoundItemResponse>>(Resource.None)
    val foundItemDetailState: StateFlow<Resource<FoundItemResponse>> = _foundItemDetailState.asStateFlow()

    // ============================================
    // Verification State Flows
    // ============================================

    private val _verifyLostItemState = MutableStateFlow<Resource<VerificationResponse>>(Resource.None)
    val verifyLostItemState: StateFlow<Resource<VerificationResponse>> = _verifyLostItemState.asStateFlow()

    private val _verifyFoundItemState = MutableStateFlow<Resource<VerificationResponse>>(Resource.None)
    val verifyFoundItemState: StateFlow<Resource<VerificationResponse>> = _verifyFoundItemState.asStateFlow()

    private val _rejectLostItemState = MutableStateFlow<Resource<String>>(Resource.None)
    val rejectLostItemState: StateFlow<Resource<String>> = _rejectLostItemState.asStateFlow()

    private val _rejectFoundItemState = MutableStateFlow<Resource<String>>(Resource.None)
    val rejectFoundItemState: StateFlow<Resource<String>> = _rejectFoundItemState.asStateFlow()

    // ============================================
    // Lost Item API Methods
    // ============================================


    /**
     * Create a new lost item with multipart image
     */
    fun createLostItemWithImage(
        title: String,
        description: String,
        category: Int,
        lostLocation: String,
        lostDate: String,
        lostTime: String,
        brand: String,
        color: String,
        size: String,
        searchTags: String,
        colorTags: String,
        materialTags: String,
        status: String,
        isVerified: Boolean,
        itemImageFile: File?
    ) {
        viewModelScope.launch {
            // Create form data parameters
            val params = mutableMapOf<String, RequestBody>()
            params["title"] = createTextPart(title)
            params["description"] = createTextPart(description)
            params["category"] = createTextPart(category.toString())
            params["lost_location"] = createTextPart(lostLocation)
            params["lost_date"] = createTextPart(lostDate)
            params["lost_time"] = createTextPart(lostTime)
            params["brand"] = createTextPart(brand)
            params["color"] = createTextPart(color)
            params["size"] = createTextPart(size)
            params["search_tags"] = createTextPart(searchTags)
            params["color_tags"] = createTextPart(colorTags)
            params["material_tags"] = createTextPart(materialTags)
            params["status"] = createTextPart(status)
            params["is_verified"] = createTextPart(isVerified.toString())

            // Create image part
            val imagePart = itemImageFile?.let {
                val requestFile = it.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("item_image", it.name, requestFile)
            }

            // Make multipart request
            (remoteRepository as RemoteRepositoryImpl).makeMultipartRequest(
                params = params,
                image = imagePart,
                endpoint = ApiEndpoints.LOST_ITEMS
            ).collectAsResource<LostItemResponse>(
                onEmit = { result ->
                    _createLostItemState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get all lost items
     */
    fun getAllLostItems() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.LOST_ITEMS,
                httpMethod = HttpMethod.GET
            ).collectAsResource<LostItemsListResponse>(
                onEmit = { result ->
                    _lostItemsListState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get lost item by ID
     */
    fun getLostItemById(id: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.LOST_ITEM_DETAIL.replace("{id}", id),
                httpMethod = HttpMethod.GET
            ).collectAsResource<LostItemResponse>(
                onEmit = { result ->
                    _lostItemDetailState.value = result
                },
                useMock = false
            )
        }
    }

    // ============================================
    // Found Item API Methods
    // ============================================

    /**
     * Create a new found item with multipart image
     */
    fun createFoundItemWithImage(
        title: String,
        description: String,
        category: Int,
        foundLocation: String,
        foundDate: String,
        foundTime: String,
        brand: String,
        color: String,
        size: String,
        searchTags: String,
        colorTags: String,
        materialTags: String,
        storageLocation: String,
        status: String,
        itemImageFile: File?
    ) {
        viewModelScope.launch {
            // Create form data parameters
            val params = mutableMapOf<String, RequestBody>()
            params["title"] = createTextPart(title)
            params["description"] = createTextPart(description)
            params["category"] = createTextPart(category.toString())
            params["found_location"] = createTextPart(foundLocation)
            params["found_date"] = createTextPart(foundDate)
            params["found_time"] = createTextPart(foundTime)
            params["brand"] = createTextPart(brand)
            params["color"] = createTextPart(color)
            params["size"] = createTextPart(size)
            params["search_tags"] = createTextPart(searchTags)
            params["color_tags"] = createTextPart(colorTags)
            params["material_tags"] = createTextPart(materialTags)
            params["storage_location"] = createTextPart(storageLocation)
            params["status"] = createTextPart(status)

            // Create image part
            val imagePart = itemImageFile?.let {
                val requestFile = it.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("item_image", it.name, requestFile)
            }

            // Make multipart request
            (remoteRepository as RemoteRepositoryImpl).makeMultipartRequest(
                params = params,
                image = imagePart,
                endpoint = ApiEndpoints.FOUND_ITEMS
            ).collectAsResource<FoundItemResponse>(
                onEmit = { result ->
                    _createFoundItemState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get all found items
     */
    fun getAllFoundItems() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.FOUND_ITEMS,
                httpMethod = HttpMethod.GET
            ).collectAsResource<FoundItemsListResponse>(
                onEmit = { result ->
                    _foundItemsListState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get found item by ID
     */
    fun getFoundItemById(id: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.FOUND_ITEM_DETAIL.replace("{id}", id),
                httpMethod = HttpMethod.GET
            ).collectAsResource<FoundItemResponse>(
                onEmit = { result ->
                    _foundItemDetailState.value = result
                },
                useMock = false
            )
        }
    }

    // ============================================
    // Admin Verification Methods
    // ============================================

    /**
     * Verify/Approve Lost Item (Admin only)
     * Endpoint: POST /api/admin/verify/lost-item/{id}/
     */
    fun verifyLostItem(itemId: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.VERIFY_LOST_ITEM.replace("{id}", itemId),
                httpMethod = HttpMethod.POST
            ).collectAsResource<VerificationResponse>(
                onEmit = { result ->
                    _verifyLostItemState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Verify/Approve Found Item (Admin only)
     * Endpoint: POST /api/admin/verify/found-item/{id}/
     */
    fun verifyFoundItem(itemId: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.VERIFY_FOUND_ITEM.replace("{id}", itemId),
                httpMethod = HttpMethod.POST
            ).collectAsResource<VerificationResponse>(
                onEmit = { result ->
                    _verifyFoundItemState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Reject Lost Item (Admin only)
     * This will delete the item or mark it as rejected
     * Endpoint: DELETE /api/lost-items/{id}/
     */
    fun deleteLostItem(itemId: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.LOST_ITEM_DELETE.replace("{id}", itemId),
                httpMethod = HttpMethod.DELETE
            ).collectAsResource<String>(
                onEmit = { result ->
                    _rejectLostItemState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Reject Found Item (Admin only)
     * This will delete the item or mark it as rejected
     * Endpoint: DELETE /api/found-items/{id}/
     */
    fun deleteFoundItem(itemId: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.FOUND_ITEM_DELETE.replace("{id}", itemId),
                httpMethod = HttpMethod.DELETE
            ).collectAsResource<String>(
                onEmit = { result ->
                    _rejectFoundItemState.value = result
                },
                useMock = false
            )
        }
    }

    // ============================================
    // Helper Methods
    // ============================================

    private fun createTextPart(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // ============================================
    // Reset Methods
    // ============================================

    fun resetCreateLostItemState() {
        _createLostItemState.value = Resource.None
    }

    fun resetLostItemsListState() {
        _lostItemsListState.value = Resource.None
    }

    fun resetLostItemDetailState() {
        _lostItemDetailState.value = Resource.None
    }

    fun resetCreateFoundItemState() {
        _createFoundItemState.value = Resource.None
    }

    fun resetFoundItemsListState() {
        _foundItemsListState.value = Resource.None
    }

    fun resetFoundItemDetailState() {
        _foundItemDetailState.value = Resource.None
    }

    fun resetVerifyLostItemState() {
        _verifyLostItemState.value = Resource.None
    }

    fun resetVerifyFoundItemState() {
        _verifyFoundItemState.value = Resource.None
    }

    fun resetRejectLostItemState() {
        _rejectLostItemState.value = Resource.None
    }

    fun resetRejectFoundItemState() {
        _rejectFoundItemState.value = Resource.None
    }

    fun resetAllStates() {
        resetCreateLostItemState()
        resetLostItemsListState()
        resetLostItemDetailState()
        resetCreateFoundItemState()
        resetFoundItemsListState()
        resetFoundItemDetailState()
        resetVerifyLostItemState()
        resetVerifyFoundItemState()
        resetRejectLostItemState()
        resetRejectFoundItemState()
    }
}