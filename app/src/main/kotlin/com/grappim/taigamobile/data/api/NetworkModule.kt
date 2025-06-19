package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.di.LocalDateTimeTypeAdapter
import com.grappim.taigamobile.di.LocalDateTypeAdapter
import com.grappim.taigamobile.login.data.AuthTokenInterceptor
import com.grappim.taigamobile.login.data.TaigaAuthenticator
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @[Provides Singleton]
    fun provideRetrofit(
        moshiConverterFactory: MoshiConverterFactory,
        session: Session,
        taigaAuthenticator: TaigaAuthenticator,
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(session.baseUrl)
        .addConverterFactory(moshiConverterFactory)
        .client(okHttpClient.newBuilder().authenticator(taigaAuthenticator).build())
        .build()

    @[Provides Singleton]
    fun provideMoshiConverterFactory(moshi: Moshi): MoshiConverterFactory =
        MoshiConverterFactory.create(moshi).withNullSerialization()

    @[Provides Singleton]
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(LocalDateTypeAdapter())
        .add(LocalDateTimeTypeAdapter())
        .build()

    @[Provides Singleton]
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authTokenInterceptor: AuthTokenInterceptor,
        hostSelectionInterceptor: HostSelectionInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(hostSelectionInterceptor)
        .addInterceptor(authTokenInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @[Provides Singleton]
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor(Timber::d)
        .setLevel(HttpLoggingInterceptor.Level.BODY)
        .also { it.redactHeader(ApiConstants.AUTHORIZATION) }
}
