package com.grappim.taigamobile.utils.formatter.decimal

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

/**
 * On taiga-front the 8 symbols after the decimal point are displayed.
 */
internal const val PATTERN_SIMPLE_DECIMAL = "###.########"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DecimalFormatSimple

interface DecimalFormatter {
    fun format(value: Double): String
}

expect fun createDecimalFormatter(): DecimalFormatter

@Module
@Configuration
@ComponentScan
object DecimalFormatterModule {
    @[Single DecimalFormatSimple]
    fun provideSimpleDecimal(): DecimalFormatter = createDecimalFormatter()
}
