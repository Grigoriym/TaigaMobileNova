package com.grappim.taigamobile.core.storage.di

import com.grappim.taigamobile.core.storage.cache.CacheManager
import com.grappim.taigamobile.core.storage.cache.CacheManagerImpl
import com.grappim.taigamobile.core.storage.cleaner.DataCleaner
import com.grappim.taigamobile.core.storage.cleaner.DataCleanerImpl
import com.grappim.taigamobile.core.storage.db.wrapper.DatabaseWrapper
import com.grappim.taigamobile.core.storage.db.wrapper.DatabaseWrapperImpl
import com.grappim.taigamobile.core.storage.network.NetworkMonitor
import com.grappim.taigamobile.core.storage.network.NetworkMonitorImpl
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.core.storage.server.ServerStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface StorageModule {

    @[Binds Singleton]
    fun bindServerStorage(impl: ServerStorageImpl): ServerStorage

    @[Binds Singleton]
    fun bindAuthStorage(impl: DatabaseWrapperImpl): DatabaseWrapper

    @[Binds Singleton]
    fun bindDataCleaner(impl: DataCleanerImpl): DataCleaner

    @[Binds Singleton]
    fun bindNetworkMonitor(impl: NetworkMonitorImpl): NetworkMonitor

    @[Binds Singleton]
    fun bindCacheManager(impl: CacheManagerImpl): CacheManager
}
