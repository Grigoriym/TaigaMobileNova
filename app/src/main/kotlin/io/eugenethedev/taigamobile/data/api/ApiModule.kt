package io.eugenethedev.taigamobile.data.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.eugenethedev.taigamobile.epics.EpicsApi
import io.eugenethedev.taigamobile.issues.IssuesApi
import io.eugenethedev.taigamobile.login.data.AuthApi
import io.eugenethedev.taigamobile.projectselector.ProjectsApi
import io.eugenethedev.taigamobile.sprint.SprintApi
import io.eugenethedev.taigamobile.userstories.UserStoriesApi
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
    fun provideEpicsApi(retrofit: Retrofit): EpicsApi =
        retrofit.create(EpicsApi::class.java)

    @[Provides Singleton]
    fun provideUserStoriesApi(retrofit: Retrofit): UserStoriesApi =
        retrofit.create(UserStoriesApi::class.java)

    @[Provides Singleton]
    fun provideSprintApi(retrofit: Retrofit): SprintApi =
        retrofit.create(SprintApi::class.java)

    @[Provides Singleton]
    fun provideIssuesApi(retrofit: Retrofit): IssuesApi =
        retrofit.create(IssuesApi::class.java)
}
