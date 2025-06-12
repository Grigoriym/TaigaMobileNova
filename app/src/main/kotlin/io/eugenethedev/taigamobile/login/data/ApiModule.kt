package io.eugenethedev.taigamobile.login.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.eugenethedev.taigamobile.data.api.TaigaApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @[Provides Singleton]
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @[Provides Singleton]
    fun provideTaigaApi(retrofit: Retrofit): TaigaApi = retrofit.create(TaigaApi::class.java)
}
