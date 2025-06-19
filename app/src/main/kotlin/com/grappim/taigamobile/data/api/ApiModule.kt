package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.feature.login.data.api.AuthApi
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.wiki.data.WikiApi
import com.grappim.taigamobile.feature.sprint.data.SprintApi
import com.grappim.taigamobile.feature.userstories.data.UserStoriesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @[Provides Singleton]
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @[Provides Singleton]
    fun provideTaigaApi(retrofit: Retrofit): TaigaApi = retrofit.create(TaigaApi::class.java)

    @[Provides Singleton]
    fun provideProjectsApi(retrofit: Retrofit): ProjectsApi =
        retrofit.create(ProjectsApi::class.java)

    @[Provides Singleton]
    fun provideEpicsApi(retrofit: Retrofit): com.grappim.taigamobile.feature.epics.data.EpicsApi =
        retrofit.create(com.grappim.taigamobile.feature.epics.data.EpicsApi::class.java)

    @[Provides Singleton]
    fun provideUserStoriesApi(retrofit: Retrofit): UserStoriesApi =
        retrofit.create(UserStoriesApi::class.java)

    @[Provides Singleton]
    fun provideSprintApi(retrofit: Retrofit): SprintApi =
        retrofit.create(SprintApi::class.java)

    @[Provides Singleton]
    fun provideIssuesApi(retrofit: Retrofit): com.grappim.taigamobile.feature.issues.data.IssuesApi =
        retrofit.create(com.grappim.taigamobile.feature.issues.data.IssuesApi::class.java)

    @[Provides Singleton]
    fun provideWIkiApi(retrofit: Retrofit): WikiApi =
        retrofit.create(WikiApi::class.java)
}
