package com.grappim.taigamobile.utils.formatter.decimal

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import javax.inject.Qualifier

@Qualifier
annotation class DecimalFormatSimple

@[Module InstallIn(SingletonComponent::class)]
object DecimalFormatterModule {
    /**
     * On taiga-front the 8 symbols after the decimal point are displayed.
     */
    private const val PATTERN_SIMPLE_DECIMAL = "###.########"

    @[Provides]
    fun provideDecimalFormatSymbols(): DecimalFormatSymbols = DecimalFormatSymbols()

    @[Provides DecimalFormatSimple]
    fun provideSimpleDecimal(decimalFormatSymbols: DecimalFormatSymbols): DecimalFormat {
        val dfs = decimalFormatSymbols.apply {
            decimalSeparator = '.'
        }
        return DecimalFormat(PATTERN_SIMPLE_DECIMAL, dfs)
    }
}
