package com.example.lostandfound.feature.category

import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.ApiEndpoints
import com.example.lostandfound.data.HttpMethod
import com.example.lostandfound.data.RemoteRepository
import com.example.lostandfound.data.Resource
import com.example.lostandfound.domain.auth.CategoryRequest
import com.example.lostandfound.domain.auth.CategoryResponse
import com.example.lostandfound.feature.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Categories
 */
class CategoryViewModel(
    private val remoteRepository: RemoteRepository
) : BaseViewModel() {

    // Create Category State
    private val _createCategoryState = MutableStateFlow<Resource<CategoryResponse>>(Resource.None)
    val createCategoryState: StateFlow<Resource<CategoryResponse>> = _createCategoryState.asStateFlow()

    // Categories List State
    private val _categoriesListState = MutableStateFlow<Resource<List<CategoryResponse>>>(Resource.None)
    val categoriesListState: StateFlow<Resource<List<CategoryResponse>>> = _categoriesListState.asStateFlow()

    /**
     * Create a new category (Admin only)
     */
    fun createCategory(name: String, description: String) {
        val request = CategoryRequest(
            name = name,
            description = description
        )

        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = request,
                endpoint = ApiEndpoints.CATEGORIES,
                httpMethod = HttpMethod.POST
            ).collectAsResource<CategoryResponse>(
                onEmit = { result ->
                    _createCategoryState.value = result
                },
                useMock = false
            )
        }
    }

    /**
     * Get all categories
     */
    fun getAllCategories() {
        viewModelScope.launch {
            remoteRepository.makeApiRequest(
                requestModel = null,
                endpoint = ApiEndpoints.CATEGORIES,
                httpMethod = HttpMethod.GET
            ).collectAsResource<List<CategoryResponse>>(
                onEmit = { result ->
                    _categoriesListState.value = result
                },
                useMock = false
            )
        }
    }

    fun resetCreateState() {
        _createCategoryState.value = Resource.None
    }

    fun resetListState() {
        _categoriesListState.value = Resource.None
    }
}
