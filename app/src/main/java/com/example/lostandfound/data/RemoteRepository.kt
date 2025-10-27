package com.example.lostandfound.data

import kotlinx.coroutines.flow.Flow


interface RemoteRepository {
    suspend fun makeApiRequest(
        requestModel: Any? = null,
        endpoint: String,
        httpMethod: HttpMethod = HttpMethod.POST,
        isEncrypted: Boolean = false,
        returnErrorBody: Boolean = false,
        isMockResponse: Boolean = false
    ): Flow<Resource<String>>
}