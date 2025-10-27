package com.example.lostandfound.data


import com.example.lostandfound.data.remote.ApiClient
import com.example.lostandfound.data.remote.ApiService

/**
 * Repository Provider - Similar to Loyalty app's Koin DI
 * Provides single instance of repositories throughout the app
 */
object RepositoryProvider {

    private val apiService: ApiService by lazy {
        ApiClient.createService(ApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService)
    }

    val itemRepository: ItemRepository by lazy {
        ItemRepository(apiService)
    }

    val claimRepository: ClaimRepository by lazy {
        ClaimRepository(apiService)
    }

    val messageRepository: MessageRepository by lazy {
        MessageRepository(apiService)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(apiService)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(apiService)
    }
}