package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface ProjectsModule {
    @[Binds Singleton]
    fun bindProjectsRepository(impl: ProjectsRepositoryImpl): ProjectsRepository
}
