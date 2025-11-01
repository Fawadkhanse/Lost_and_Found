package com.example.lostandfound.di

import com.example.lostandfound.data.remote.ApiClient
import com.example.lostandfound.data.remote.ApiService
import com.example.lostandfound.domain.repository.RemoteRepository
import com.example.lostandfound.data.repo.RemoteRepositoryImpl
import com.example.lostandfound.feature.auth.AuthViewModel
import com.example.lostandfound.feature.category.CategoryViewModel
import com.example.lostandfound.feature.home.DashboardViewModel
import com.example.lostandfound.feature.item.ClaimViewModel
import com.example.lostandfound.feature.item.ItemViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

// Network Module - Updated to use context for Chucker
val networkModule = module {
    // Provide Retrofit instance with context for Chucker
    single<Retrofit> {
        ApiClient.getRetrofit(androidContext())
    }

    // Provide ApiService
    single<ApiService> {
        get<Retrofit>().create(ApiService::class.java)
    }
}

// Repository Module
val repositoryModule = module {
    single<RemoteRepository> {
        RemoteRepositoryImpl(
            context = androidContext(),
            apiService = get()
        )
    }
}

// ViewModel Module - Updated with all ViewModels
val viewModelModule = module {
    // Authentication ViewModel
    viewModel { AuthViewModel(get()) }

    // Claim ViewModel
    viewModel { ClaimViewModel(get()) }

    // Category ViewModel
    viewModel { CategoryViewModel(get()) }

    // Dashboard ViewModel
    viewModel { DashboardViewModel(get()) }

    // Item ViewModel
    viewModel { ItemViewModel(get()) }
}

// Combined modules list
val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule
)