package com.example.lostandfound.data.remote


import android.content.Context
import android.util.Log
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.example.lostandfound.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClient - Retrofit configuration
 * Provides configured Retrofit instance with Chucker integration
 */
object ApiClient {
    private const val BASE_URL = "https://lost-found-project-1-f795.onrender.com/api/"
    private const val TIMEOUT = 60L

    @Volatile
    private var retrofit: Retrofit? = null

    /**
     * Get or create Retrofit instance
     * Call this method to get the Retrofit instance
     */
    fun getRetrofit(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: createRetrofit(context).also { retrofit = it }
        }
    }

    private fun createRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createOkHttpClient(context: Context): OkHttpClient {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(createLoggingInterceptor())
            .addInterceptor(createHeaderInterceptor())
            .addInterceptor(createHeaderLoggingInterceptor())
        if (BuildConfig.DEBUG) {
            okHttp.addInterceptor(getChuckerConfigration(context))
        }
        return okHttp.build()
    }

    /**
     * Create Chucker interceptor for network debugging
     */
    private fun getChuckerConfigration(context: Context): ChuckerInterceptor {
        val chuckerCollector = ChuckerCollector(
            context = context,
            // Toggles visibility of the notification
            showNotification = true,
            // Allows to customize the retention period of collected data
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )


        val chuckerInterceptor = ChuckerInterceptor.Builder(context)
            .collector(chuckerCollector)
            .maxContentLength(450_000L)
            .redactHeaders("Auth-Token", "Bearer")
            .alwaysReadResponseBody(true)
            .createShortcut(true)

        return chuckerInterceptor.build()
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
     * Create header logging interceptor
     */
    private fun createHeaderLoggingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            // Log all request headers
            Log.d("ApiClient", "➡️ REQUEST ${request.method} ${request.url}")
            for ((name, value) in request.headers) {
                Log.d("ApiClient", "   🔹 $name: $value")
            }

            val response = chain.proceed(request)

            // Log all response headers
            Log.d("ApiClient", "⬅️ RESPONSE ${response.code} ${response.message}")
            for ((name, value) in response.headers) {
                Log.d("ApiClient", "   🔸 $name: $value")
            }

            response
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
                Log.d("ApiClient", "Adding Authorization token")
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
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