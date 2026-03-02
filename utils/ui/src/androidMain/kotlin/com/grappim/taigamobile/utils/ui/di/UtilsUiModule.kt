package com.grappim.taigamobile.utils.ui.di

import com.grappim.taigamobile.utils.ui.ColorMapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
class UtilsUiModule {

    @Factory
    fun colorMapper(): ColorMapper = ColorMapper()
}
