package com.example.lostandfound.domain.repository

import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.data.Resource
import kotlinx.coroutines.flow.Flow

interface RemoteRepository {
    suspend fun makeApiRequest(
        requestModel: Any? = null,
        endpoint: String,
        httpMethod: HttpMethod = HttpMethod.POST,
        returnErrorBody: Boolean = false,
        isMockResponse: Boolean = false
    ): Flow<Resource<String>>
}