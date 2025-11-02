package com.example.lostandfound.feature.claimitem

import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.Resource
import com.example.lostandfound.data.remote.ApiEndpoints
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.domain.auth.ClaimRequest
import com.example.lostandfound.domain.auth.ClaimResponse
import com.example.lostandfound.domain.auth.ClaimsListResponse
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Claims
 * Using your existing architecture pattern
 */
class ClaimViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    // ============================================
    // State Flows
    // ============================================

    // Create Claim State
    private val _createClaimState = MutableStateFlow<Resource<ClaimResponse>>(Resource.None)
    val createClaimState: StateFlow<Resource<ClaimResponse>> = _createClaimState.asStateFlow()

    // Claims List State
    private val _claimsListState = MutableStateFlow<Resource<ClaimsListResponse>>(Resource.None)
    val claimsListState: StateFlow<Resource<ClaimsListResponse>> = _claimsListState.asStateFlow()

    // Single Claim State
    private val _claimDetailState = MutableStateFlow<Resource<ClaimResponse>>(Resource.None)
    val claimDetailState: StateFlow<Resource<ClaimResponse>> = _claimDetailState.asStateFlow()

    // Update Claim State
    private val _updateClaimState = MutableStateFlow<Resource<ClaimResponse>>(Resource.None)
    val updateClaimState: StateFlow<Resource<ClaimResponse>> = _updateClaimState.asStateFlow()

    // ============================================
    // API Methods
    // ============================================

    /**
     * Create a new claim
     */
    fun createClaim(
        foundItem: String,
        claimDescription: String,
        proofOfOwnership: String,
        supportingImages: String? = null,
        status: String = "pending",
        adminNotes: String? = null
    ) {
        val request = ClaimRequest(
            foundItem = foundItem,
            claimDescription = claimDescription,
            proofOfOwnership = proofOfOwnership,
            supportingImages = supportingImages,
            status = status,
            adminNotes = adminNotes
        )

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.CLAIMS,
                httpMethod = HttpMethod.POST
            ).collectAsResource<ClaimResponse>(
                onEmit = { result ->
                    _createClaimState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get all claims
     */
    fun getAllClaims() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.CLAIMS,
                httpMethod = HttpMethod.GET
            ).collectAsResource<ClaimsListResponse>(
                onEmit = { result ->
                    _claimsListState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get claim by ID
     */
    fun getClaimById(id: String) {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.CLAIM_DETAIL.replace("{id}", id),
                httpMethod = HttpMethod.GET
            ).collectAsResource<ClaimResponse>(
                onEmit = { result ->
                    _claimDetailState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Update claim
     */
    fun updateClaim(
        id: String,
        foundItem: String,
        claimDescription: String,
        proofOfOwnership: String,
        supportingImages: String? = null,
        status: String,
        adminNotes: String? = null
    ) {
        val request = ClaimRequest(
            foundItem = foundItem,
            claimDescription = claimDescription,
            proofOfOwnership = proofOfOwnership,
            supportingImages = supportingImages,
            status = status,
            adminNotes = adminNotes
        )

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.CLAIM_DETAIL.replace("{id}", id),
                httpMethod = HttpMethod.PUT
            ).collectAsResource<ClaimResponse>(
                onEmit = { result ->
                    _updateClaimState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Reset states
     */
    fun resetCreateState() {
        _createClaimState.value = Resource.None
    }

    fun resetListState() {
        _claimsListState.value = Resource.None
    }

    fun resetDetailState() {
        _claimDetailState.value = Resource.None
    }

    fun resetUpdateState() {
        _updateClaimState.value = Resource.None
    }
}