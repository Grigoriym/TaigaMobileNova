package com.grappim.taigamobile.composeapp.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

@Module
expect class PlatformComponentModule

@Module
@Configuration
@ComponentScan("com.grappim.taigamobile")
class AppModule

@KoinApplication
object KoinApp
