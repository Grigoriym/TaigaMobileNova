package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface WikiModule {

    @[Binds Singleton]
    fun bindIWikiRepository(wikiRepositoryImpl: WikiRepositoryImpl): WikiRepository
}
