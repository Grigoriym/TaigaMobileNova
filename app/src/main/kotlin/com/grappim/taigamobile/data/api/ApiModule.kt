package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.api.AuthRetrofit
import com.grappim.taigamobile.core.api.CommonRetrofit
import com.grappim.taigamobile.feature.epics.data.EpicsApi
import com.grappim.taigamobile.feature.filters.data.FiltersApi
import com.grappim.taigamobile.feature.history.data.HistoryApi
import com.grappim.taigamobile.feature.issues.data.IssuesApi
import com.grappim.taigamobile.feature.login.data.api.AuthApi
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.sprint.data.SprintApi
import com.grappim.taigamobile.feature.swimlanes.data.SwimlanesApi
import com.grappim.taigamobile.feature.userstories.data.UserStoriesApi
import com.grappim.taigamobile.feature.wiki.data.WikiApi
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
    fun provideAuthApi(@AuthRetrofit retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @[Provides Singleton]
    fun provideTaigaApi(@CommonRetrofit retrofit: Retrofit): TaigaApi =
        retrofit.create(TaigaApi::class.java)

    @[Provides Singleton]
    fun provideProjectsApi(@CommonRetrofit retrofit: Retrofit): ProjectsApi =
        retrofit.create(ProjectsApi::class.java)

    @[Provides Singleton]
    fun provideEpicsApi(@CommonRetrofit retrofit: Retrofit): EpicsApi =
        retrofit.create(EpicsApi::class.java)

    @[Provides Singleton]
    fun provideUserStoriesApi(@CommonRetrofit retrofit: Retrofit): UserStoriesApi =
        retrofit.create(UserStoriesApi::class.java)

    @[Provides Singleton]
    fun provideSprintApi(@CommonRetrofit retrofit: Retrofit): SprintApi =
        retrofit.create(SprintApi::class.java)

    @[Provides Singleton]
    fun provideIssuesApi(@CommonRetrofit retrofit: Retrofit): IssuesApi =
        retrofit.create(IssuesApi::class.java)

    @[Provides Singleton]
    fun provideWikiApi(@CommonRetrofit retrofit: Retrofit): WikiApi =
        retrofit.create(WikiApi::class.java)

    @[Provides Singleton]
    fun provideFiltersApi(@CommonRetrofit retrofit: Retrofit): FiltersApi =
        retrofit.create(FiltersApi::class.java)

    @[Provides Singleton]
    fun provideSwimlanesApi(@CommonRetrofit retrofit: Retrofit): SwimlanesApi =
        retrofit.create(SwimlanesApi::class.java)

    @[Provides Singleton]
    fun provideHistoryApi(@CommonRetrofit retrofit: Retrofit): HistoryApi =
        retrofit.create(HistoryApi::class.java)
}
