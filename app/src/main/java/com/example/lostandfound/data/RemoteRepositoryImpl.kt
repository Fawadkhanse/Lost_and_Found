package com.example.lostandfound.data

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
            // Make network request using Retrofit
            val response: Response<Any> = when (httpMethod) {
                HttpMethod.GET -> if (requestModel != null)
                    apiService.getWithBody(endpoint, requestModel)
                else apiService.get(endpoint)

                HttpMethod.POST -> if (requestModel != null)
                    apiService.post(endpoint, requestModel)
                else apiService.post(endpoint)

                HttpMethod.PUT -> apiService.put(endpoint, requestModel ?: Any())
                HttpMethod.DELETE -> apiService.delete(endpoint)
            }

            // Mock mode shortcut
            if (isMock) {
                emit(Resource.Success("00",))
                return@flow
            }

            // Handle successful response
            if (response.isSuccessful) {
                val body = gson.toJson(response.body() ?: "{}")
                emit(Resource.Success( body))
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                val message = parseErrorMessage(errorBody)

                if (returnErrorBody)
                    emit(Resource.Success("${errorBody}"))
                else {
                    val errorCode = response.code().toString()
                    emit(Resource.Error(Exception(errorCode)))
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(exception = Exception(e.message ?: "Unexpected error", )))
        }
    }.catch { e ->
        emit(Resource.Error(Exception(e.message ?: "Network error", )))
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            val map = gson.fromJson(errorBody, Map::class.java)
            map["message"]?.toString() ?: "Request failed"
        } catch (e: Exception) {
            errorBody.ifBlank { "Request failed" }
        }
    }
}
