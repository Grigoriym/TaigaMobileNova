package com.grappim.taigamobile.feature.tasks.data

import com.grappim.taigamobile.core.api.CommonRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
object TasksModule {
    @[Provides Singleton]
    fun provideTasksApi(@CommonRetrofit retrofit: Retrofit): TasksApi =
        retrofit.create(TasksApi::class.java)
}
