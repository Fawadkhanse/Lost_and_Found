package com.example.lostandfound.data

sealed class Resource<out T> {
    data class Loading(val isLoading: Boolean) : Resource<Nothing>()
    data class Success<T>(val code: String, val data: T) : Resource<T>()
    data class Failure(val code: String, val message: String, val error: String?) : Resource<Nothing>()
    object None : Resource<Nothing>()
}