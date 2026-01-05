package com.grappim.taigamobile.core.storage.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class StorageJsonQualifier

@[Module InstallIn(SingletonComponent::class)]
class StorageJsonProver @Inject constructor() {

    @[Provides Singleton StorageJsonQualifier]
    fun provideStorageJson(): Json = Json {
        ignoreUnknownKeys = true
    }
}
