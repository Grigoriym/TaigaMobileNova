package com.grappim.taigamobile.di

import com.grappim.taigamobile.data.AppUpdateModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [AppUpdateModule::class])
@Configuration
@ComponentScan("com.grappim.taigamobile.data")
class AndroidModule
