package com.example.lostandfound.data


import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader
import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader.TIMEOUT
import com.example.lostandfound.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClient - Retrofit configuration
 * Provides configured Retrofit instance
 */
object ApiClient {

    private const val BASE_URL = "https://your-api-url.com/api/"

        fun createRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        private fun createOkHttpClient(): OkHttpClient {
             val TIMEOUT = 60L
            return OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(createLoggingInterceptor())
                .addInterceptor(createHeaderInterceptor())  // Adds auth token
                .build()
        }
    }


    /**
     * Create logging interceptor
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Create header interceptor
     * Add common headers to all requests
     */
    private fun createHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")

            // Add authorization token if available
            TokenManager.getToken()?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }


/**
 * Token Manager - Manage authentication token
 */
object TokenManager {
    private var token: String? = null

    fun setToken(token: String?) {
        this.token = token
    }

    fun getToken(): String? = token

    fun clearToken() {
        token = null
    }
}
enum class HttpMethod { GET, POST, PUT, DELETE }