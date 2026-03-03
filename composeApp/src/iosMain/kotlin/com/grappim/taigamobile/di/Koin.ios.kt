package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.api.KmpNetworkModule
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeModule
import com.grappim.taigamobile.utils.formatter.decimal.DecimalFormatterModule
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(
    includes = [
        AppModule::class,
        KmpNetworkModule::class,
        DateTimeModule::class,
        DecimalFormatterModule::class,
    ]
)
@Configuration
actual class PlatformComponentModule
