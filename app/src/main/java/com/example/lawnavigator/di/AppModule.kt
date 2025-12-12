package com.example.lawnavigator.di

import com.example.lawnavigator.BuildConfig
import com.example.lawnavigator.data.api.AuthApi
import com.example.lawnavigator.data.api.ContentApi
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.data.repository.AuthRepositoryImpl
import com.example.lawnavigator.data.repository.ContentRepositoryImpl
import com.example.lawnavigator.domain.repository.AuthRepository
import com.example.lawnavigator.domain.repository.ContentRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {



    @Provides
    @Singleton
    fun provideContentRepository(
        api: ContentApi,
        tokenManager: TokenManager
    ): ContentRepository {
        return ContentRepositoryImpl(api, tokenManager)
    }

    @Provides
    @Singleton
    fun provideContentApi(retrofit: Retrofit): ContentApi {
        return retrofit.create(ContentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Чтобы видеть JSON в Logcat
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            // БЕРЕМ ИЗ КОНФИГА (он сам выберет нужный адрес)
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }


    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApi,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepositoryImpl(api, tokenManager)
    }

}