package com.grappim.taigamobile.data.api

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.grappim.taigamobile.core.api.BaseUrlProvider
import com.grappim.taigamobile.core.api.CommonOkHttp
import com.grappim.taigamobile.core.api.CommonRetrofit
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.data.interceptors.ErrorMappingInterceptor
import com.grappim.taigamobile.data.interceptors.HostSelectionInterceptor
import com.grappim.taigamobile.data.interceptors.TaigaBearerTokenAuthenticator
import com.grappim.taigamobile.di.LocalDateTimeTypeAdapter
import com.grappim.taigamobile.di.LocalDateTypeAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @[Provides CommonRetrofit Singleton]
    fun provideRetrofit(
        moshiConverterFactory: MoshiConverterFactory,
        baseUrlProvider: BaseUrlProvider,
        @CommonOkHttp okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrlProvider.getBaseUrl())
        .addConverterFactory(moshiConverterFactory)
        .client(okHttpClient)
        .build()

    @[Provides CommonOkHttp Singleton]
    fun provideCommonOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        authTokenProviderInterceptor: AuthTokenProviderInterceptor,
        hostSelectionInterceptor: HostSelectionInterceptor,
        appInfoProvider: AppInfoProvider,
        taigaBearerTokenAuthenticator: TaigaBearerTokenAuthenticator,
        errorMappingInterceptor: ErrorMappingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(getOkHttpTimeout(appInfoProvider), TimeUnit.SECONDS)
        .readTimeout(getOkHttpTimeout(appInfoProvider), TimeUnit.SECONDS)
        .addInterceptor(errorMappingInterceptor)
        .addInterceptor(hostSelectionInterceptor)
        .addInterceptor(authTokenProviderInterceptor)
        .authenticator(taigaBearerTokenAuthenticator)
        .apply {
            if (appInfoProvider.isDebug()) {
                addInterceptor(loggingInterceptor)
                addInterceptor(chuckerInterceptor)
            }
        }
        .build()

    @[Provides Singleton]
    fun provideMoshiConverterFactory(moshi: Moshi): MoshiConverterFactory =
        MoshiConverterFactory.create(moshi).withNullSerialization()

    @[Provides Singleton]
    fun provideMoshi(localDateTypeAdapter: LocalDateTypeAdapter): Moshi = Moshi.Builder()
        .add(localDateTypeAdapter)
        .add(LocalDateTimeTypeAdapter())
        .build()

    @[Provides Singleton]
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor(Timber::d)
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    /**
     * Should be turned on in the App Info once you enable notifications
     * Maybe later we will ask for permissions on debug
     */
    @[Provides Singleton]
    fun provideChuckerInterceptor(@ApplicationContext appContext: Context): ChuckerInterceptor {
        val chuckerCollector = ChuckerCollector(
            context = appContext,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )
        return ChuckerInterceptor.Builder(context = appContext)
            .collector(collector = chuckerCollector)
            .maxContentLength(
                length = 250000L
            )
            .redactHeaders(emptySet())
            .alwaysReadResponseBody(true)
            .build()
    }
}
