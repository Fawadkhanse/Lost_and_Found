package com.example.lostandfound.feature.item

import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.ApiEndpoints
import com.example.lostandfound.data.HttpMethod
import com.example.lostandfound.data.RemoteRepository
import com.example.lostandfound.data.Resource
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

/**
 * ViewModel for managing Lost and Found Items
 */
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
    // Lost Item API Methods
    // ============================================

    /**
     * Create a new lost item
     */
    fun createLostItem(request: LostItemRequest) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.LOST_ITEMS,
                httpMethod = HttpMethod.POST
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
     * Create a new found item
     */
    fun createFoundItem(request: FoundItemRequest) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.FOUND_ITEMS,
                httpMethod = HttpMethod.POST
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

    fun resetAllStates() {
        resetCreateLostItemState()
        resetLostItemsListState()
        resetLostItemDetailState()
        resetCreateFoundItemState()
        resetFoundItemsListState()
        resetFoundItemDetailState()
    }
}