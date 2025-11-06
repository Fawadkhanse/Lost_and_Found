// app/src/main/java/com/example/lostandfound/data/repo/RemoteRepositoryImpl.kt
package com.example.lostandfound.data.repo

import android.content.Context
import com.example.lostandfound.data.remote.ApiService
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.data.Resource
import com.example.lostandfound.domain.auth.ErrorResponse
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.utils.toPojo
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class RemoteRepositoryImpl(
    private val context: Context,
    private val apiService: ApiService
) : RemoteRepository {

    private val gson = Gson()

    override suspend fun makeApiRequest(
        requestModel: Any?,
        endpoint: String,
        httpMethod: HttpMethod,
        returnErrorBody: Boolean,
        isMock: Boolean
    ): Flow<Resource<String>> = flow {

        emit(Resource.Loading)

        try {
            val response: Response<Any> = when (httpMethod) {
                HttpMethod.POST -> apiService.post(endpoint, requestModel ?: Any())
                HttpMethod.GET -> apiService.get(endpoint)
                HttpMethod.PUT -> apiService.put(endpoint, requestModel ?: Any())
                HttpMethod.DELETE -> apiService.delete(endpoint)
            }

            if (response.isSuccessful) {
                val body = gson.toJson(response.body() ?: "{}")
                emit(Resource.Success(body))
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                try {
                    val errorResponse = errorBody.toPojo<com.example.lostandfound.domain.ErrorResponse>()
                    val message = errorResponse.detail?.firstOrNull() ?: "Unknown error occurred"
                    emit(Resource.Error(Exception(message)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(Resource.Error(Exception("Something went wrong")))
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(Exception(e.message ?: "Unexpected error")))
        }
    }.catch { e ->
        e.printStackTrace()
        emit(Resource.Error(Exception(e.message ?: "Network error")))
    }.flowOn(Dispatchers.IO)

    // New method for multipart requests
    override suspend fun makeMultipartRequest(
        params: Map<String, RequestBody>,
        image: MultipartBody.Part?,
        endpoint: String,
        httpMethod: HttpMethod,
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading)

        try {
            val response: Response<Any> = when (httpMethod) {
                HttpMethod.POST -> apiService.postMultipart(endpoint, params, image)
                HttpMethod.PUT -> apiService.putMultipart(endpoint, params, image)
                else -> throw IllegalArgumentException("Invalid HTTP method")
            }
            if (response.isSuccessful) {
                val body = gson.toJson(response.body() ?: "{}")
                emit(Resource.Success(body))
            } else {
                val errorBody = response.errorBody()?.string() ?: ""

                try {
                    val errorResponse = errorBody.toPojo<com.example.lostandfound.domain.ErrorResponse>()
                    val message = errorResponse.detail?.firstOrNull() ?: "Unknown error occurred"
                    emit(Resource.Error(Exception(message)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(Resource.Error(Exception("Something went wrong")))
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(Exception(e.message ?: "Unexpected error")))
        }
    }.catch { e ->
        e.printStackTrace()
        emit(Resource.Error(Exception(e.message ?: "Network error")))
    }.flowOn(Dispatchers.IO)
}