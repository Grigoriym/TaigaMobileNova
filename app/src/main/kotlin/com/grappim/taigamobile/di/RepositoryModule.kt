package com.grappim.taigamobile.di

import com.grappim.taigamobile.data.repositories.ProjectsRepository
import com.grappim.taigamobile.data.repositories.TasksRepository
import com.grappim.taigamobile.data.repositories.UsersRepository
import com.grappim.taigamobile.data.repositories.WikiRepository
import com.grappim.taigamobile.domain.repositories.IProjectsRepository
import com.grappim.taigamobile.domain.repositories.ITasksRepository
import com.grappim.taigamobile.domain.repositories.IUsersRepository
import com.grappim.taigamobile.domain.repositories.IWikiRepository
import com.grappim.taigamobile.epics.EpicsRepository
import com.grappim.taigamobile.epics.EpicsRepositoryImpl
import com.grappim.taigamobile.login.data.AuthRepository
import com.grappim.taigamobile.login.domain.IAuthRepository
import com.grappim.taigamobile.sprint.ISprintsRepository
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
    fun bindIAuthRepository(authRepository: AuthRepository): IAuthRepository

    @Singleton
    @Binds
    fun bindIProjectsRepository(searchRepository: ProjectsRepository): IProjectsRepository

    @Singleton
    @Binds
    fun bindIStoriesRepository(storiesRepository: TasksRepository): ITasksRepository

    @Singleton
    @Binds
    fun bindIUsersRepository(usersRepository: UsersRepository): IUsersRepository

    @Singleton
    @Binds
    fun bindISprintsRepository(sprintsRepository: SprintsRepository): ISprintsRepository

    @Singleton
    @Binds
    fun bindIWikiRepository(wikiRepository: WikiRepository): IWikiRepository

    @Singleton
    @Binds
    fun bindEpicsRepository(epicsRepository: EpicsRepositoryImpl): EpicsRepository
}
