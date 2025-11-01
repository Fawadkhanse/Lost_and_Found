package com.example.lostandfound.data.remote


import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service interface for Retrofit
 * Supports all HTTP methods
 */
interface ApiService {

    @GET
    suspend fun get(@Url url: String): Response<Any>

    @GET
    suspend fun getWithBody(@Url url: String, @Body body: Any): Response<Any>

    @POST
    suspend fun post(@Url url: String): Response<Any>

    @POST
    suspend fun post(@Url url: String, @Body body: Any): Response<Any>

    @PUT
    suspend fun put(@Url url: String, @Body body: Any): Response<Any>

    @DELETE
    suspend fun delete(@Url url: String): Response<Any>

    @PATCH
    suspend fun patch(@Url url: String, @Body body: Any): Response<Any>
    @Multipart
    @POST
    suspend fun postMultipart(
        @Url url: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<Any>

}