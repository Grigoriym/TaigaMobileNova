package io.eugenethedev.taigamobile.issues

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface IssuesModule {
    @Binds
    @Singleton
    fun bindIssuesRepository(impl: IssuesRepositoryImpl): IssuesRepository
}
