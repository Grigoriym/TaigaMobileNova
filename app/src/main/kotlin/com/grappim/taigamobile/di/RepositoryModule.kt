package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.domain.TasksRepository
import com.grappim.taigamobile.data.repositories.TasksRepositoryImpl
import com.grappim.taigamobile.data.repositories.UsersRepositoryImpl
import com.grappim.taigamobile.feature.projects.data.ProjectsRepositoryImpl
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.sprint.domain.ISprintsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.wiki.data.WikiRepository
import com.grappim.taigamobile.feature.wiki.domain.IWikiRepository
import com.grappim.taigamobile.sprint.SprintsRepository
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
    fun bindIUsersRepository(usersRepositoryImpl: UsersRepositoryImpl): UsersRepository

    @Singleton
    @Binds
    fun bindISprintsRepository(sprintsRepository: SprintsRepository): ISprintsRepository

    @Singleton
    @Binds
    fun bindIWikiRepository(wikiRepository: WikiRepository): IWikiRepository
}
