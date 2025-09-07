package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.issues.domain.PatchDataGenerator
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

    @Binds
    fun bindPatchDataGenerator(impl: PatchDataGeneratorImpl): PatchDataGenerator
}
