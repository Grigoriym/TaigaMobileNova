package com.grappim.taigamobile.utils.formatter.datetime

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@ComponentScan
@Configuration
class DateTimeModule {
    @Factory
    fun provideDateTimeUtils(): DateTimeUtils = DateTimeUtilsImpl(
        KotlinxDateTimeFormatter()
    )
}
