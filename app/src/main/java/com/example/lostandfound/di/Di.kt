package com.example.lostandfound.di

import com.example.lostandfound.data.ApiClient
import com.example.lostandfound.data.ApiService
import com.example.lostandfound.data.RemoteRepository
import com.example.lostandfound.data.RemoteRepositoryImpl
import com.example.lostandfound.feature.auth.AuthViewModel

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

// Network Module
val networkModule = module {
    single { ApiClient.createRetrofit() }
    single { get<Retrofit>().create(ApiService::class.java) }
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

// ViewModel Module
val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
}

// Combined modules list
val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule
)