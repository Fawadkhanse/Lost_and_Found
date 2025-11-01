// app/src/main/java/com/example/lostandfound/data/repo/RemoteRepositoryImpl.kt
package com.example.lostandfound.data.repo

import android.content.Context
import com.example.lostandfound.data.remote.ApiService
import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.data.Resource
import com.example.lostandfound.domain.repository.RemoteRepository
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
                emit(Resource.Error(Exception(errorBody)))
            }

        } catch (e: Exception) {
            emit(Resource.Error(Exception(e.message ?: "Unexpected error")))
        }
    }.catch { e ->
        emit(Resource.Error(Exception(e.message ?: "Network error")))
    }.flowOn(Dispatchers.IO)

    // New method for multipart requests
    override suspend fun makeMultipartRequest(
        params: Map<String, RequestBody>,
        image: MultipartBody.Part?,
        endpoint: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading)

        try {
            val response = apiService.postMultipart(endpoint, params, image)

            if (response.isSuccessful) {
                val body = gson.toJson(response.body() ?: "{}")
                emit(Resource.Success(body))
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                emit(Resource.Error(Exception(errorBody)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(Exception(e.message ?: "Unexpected error")))
        }
    }.catch { e ->
        emit(Resource.Error(Exception(e.message ?: "Network error")))
    }.flowOn(Dispatchers.IO)
}