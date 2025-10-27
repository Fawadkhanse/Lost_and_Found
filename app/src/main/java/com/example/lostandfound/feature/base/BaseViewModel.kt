package com.example.lostandfound.feature.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.Resource
import com.example.lostandfound.utils.toPojoOrNull
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    /**
     * Collects a Flow<Resource<String>> and converts its JSON data to the given type [T].
     * Supports mock responses for testing without API calls.
     */
    protected suspend inline fun <reified T> Flow<Resource<String>>.collectAsResource(
        crossinline onEmit: (Resource<T>) -> Unit,
        mockResponse: T? = null,
        useMock: Boolean = false
    ) {
        if (useMock && mockResponse != null) {
            onEmit(Resource.Success(mockResponse))
            return
        }

        this.map { resource ->
            when (resource) {
                is Resource.Success -> {
                    try {
                        val parsed = resource.data.toPojoOrNull<T>()
                        parsed?.let { Resource.Success(it) }
                            ?: Resource.Error(Exception("Failed to parse response"))
                    } catch (e: Exception) {
                        Resource.Error(e)
                    }
                }

                is Resource.Error -> Resource.Error(resource.exception)
                is Resource.Loading -> Resource.Loading
                Resource.None -> Resource.None
            }
        }.collect { mapped ->
            onEmit(mapped)
        }
    }

    /**
     * Launches a coroutine safely in [viewModelScope].
     * Automatically catches exceptions using [CoroutineExceptionHandler].
     */
    protected fun launchApi(block: suspend () -> Unit) {
        val handler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
        }

        viewModelScope.launch(handler) {
            block()
        }
    }
}
