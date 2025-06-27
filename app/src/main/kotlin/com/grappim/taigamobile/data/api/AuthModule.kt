package com.grappim.taigamobile.data.api

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.grappim.taigamobile.core.api.AuthOkHttp
import com.grappim.taigamobile.core.api.AuthRetrofit
import com.grappim.taigamobile.core.api.BaseUrlProvider
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.data.interceptors.ErrorMappingInterceptor
import com.grappim.taigamobile.data.interceptors.HostSelectionInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Common module for auth, refresh requests where we don't have authentication token
 * and where we don't need authenticator
 */
@[Module InstallIn(SingletonComponent::class)]
object AuthModule {
    @[Provides AuthRetrofit Singleton]
    fun provideAuthRetrofit(
        moshiConverterFactory: MoshiConverterFactory,
        baseUrlProvider: BaseUrlProvider,
        @AuthOkHttp okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrlProvider.getBaseUrl())
        .addConverterFactory(moshiConverterFactory)
        .client(okHttpClient)
        .build()

    @[Provides AuthOkHttp Singleton]
    fun provideAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        hostSelectionInterceptor: HostSelectionInterceptor,
        appInfoProvider: AppInfoProvider,
        errorMappingInterceptor: ErrorMappingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(getOkHttpTimeout(appInfoProvider), TimeUnit.SECONDS)
        .readTimeout(getOkHttpTimeout(appInfoProvider), TimeUnit.SECONDS)
        .addInterceptor(errorMappingInterceptor)
        .addInterceptor(hostSelectionInterceptor)
        .apply {
            if (appInfoProvider.isDebug()) {
                addInterceptor(loggingInterceptor)
                addInterceptor(chuckerInterceptor)
            }
        }
        .build()
}
