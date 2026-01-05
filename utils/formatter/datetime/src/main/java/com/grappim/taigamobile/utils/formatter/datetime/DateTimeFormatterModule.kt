package com.grappim.taigamobile.utils.formatter.datetime

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Qualifier

@Qualifier
annotation class LocalDateUIMedium

@Qualifier
annotation class IsoLocalDateFormatter

@[Module InstallIn(SingletonComponent::class)]
interface DateTimeFormatterModule {

    @Binds
    fun bindDateTimeUtils(impl: DateTimeUtilsImpl): DateTimeUtils

    companion object {
        @[Provides LocalDateUIMedium]
        fun provideLocalDateUiMedium(): DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

        @[Provides IsoLocalDateFormatter]
        fun provideIsoLocalDateFormatter(): DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
