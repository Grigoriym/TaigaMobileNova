package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.domain.TasksRepository
import com.grappim.taigamobile.data.repositories.TasksRepositoryImpl
import com.grappim.taigamobile.feature.projects.data.ProjectsRepositoryImpl
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.wiki.data.WikiRepositoryImpl
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindIProjectsRepository(searchRepository: ProjectsRepositoryImpl): ProjectsRepository

    @Singleton
    @Binds
    fun bindIStoriesRepository(storiesRepository: TasksRepositoryImpl): TasksRepository

    @Singleton
    @Binds
    fun bindIWikiRepository(wikiRepositoryImpl: WikiRepositoryImpl): WikiRepository
}
