package com.example.lostandfound.domain.repository

import com.example.lostandfound.data.remote.HttpMethod
import com.example.lostandfound.data.Resource
import kotlinx.coroutines.flow.Flow


import okhttp3.MultipartBody
import okhttp3.RequestBody

interface RemoteRepository {
    suspend fun makeApiRequest(
        requestModel: Any? = null,
        endpoint: String,
        httpMethod: HttpMethod = HttpMethod.POST,
        returnErrorBody: Boolean = false,
        isMockResponse: Boolean = false
    ): Flow<Resource<String>>

    suspend fun makeMultipartRequest(
        params: Map<String, RequestBody>,
        image: MultipartBody.Part?,
        endpoint: String,
        httpMethod: HttpMethod = HttpMethod.POST,
    ): Flow<Resource<String>>
}