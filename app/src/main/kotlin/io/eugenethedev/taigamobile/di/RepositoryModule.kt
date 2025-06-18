package io.eugenethedev.taigamobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.eugenethedev.taigamobile.data.repositories.ProjectsRepository
import io.eugenethedev.taigamobile.sprint.SprintsRepository
import io.eugenethedev.taigamobile.data.repositories.TasksRepository
import io.eugenethedev.taigamobile.data.repositories.UsersRepository
import io.eugenethedev.taigamobile.data.repositories.WikiRepository
import io.eugenethedev.taigamobile.domain.repositories.IProjectsRepository
import io.eugenethedev.taigamobile.sprint.ISprintsRepository
import io.eugenethedev.taigamobile.domain.repositories.ITasksRepository
import io.eugenethedev.taigamobile.domain.repositories.IUsersRepository
import io.eugenethedev.taigamobile.domain.repositories.IWikiRepository
import io.eugenethedev.taigamobile.epics.EpicsRepository
import io.eugenethedev.taigamobile.epics.EpicsRepositoryImpl
import io.eugenethedev.taigamobile.login.data.AuthRepository
import io.eugenethedev.taigamobile.login.domain.IAuthRepository
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
