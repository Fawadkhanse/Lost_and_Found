package com.example.lostandfound.feature.base

import androidx.lifecycle.ViewModel
import com.example.lostandfound.data.Resource
import kotlinx.coroutines.flow.Flow

abstract class BaseViewModel : ViewModel() {
    protected suspend inline fun <reified T> Flow<Resource<String>>.collectAsResource(
        crossinline onEmit: (Resource<T>) -> Unit,
        mockResponse: T? = null,
        useMock: Boolean = false
    ) {
    }

    protected fun launchApi(block: suspend () -> Unit) {}
}