package com.grappim.taigamobile

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.grappim.taigamobile.di.KoinApp
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin
import org.koin.plugin.module.dsl.startKoin
import org.koin.test.check.checkModules
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class KoinGraphTest {

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun verifyKoinGraph() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        startKoin<KoinApp> {
            androidContext(context)
        }.checkModules {
            withInstance<Context>(context)
            withInstance<Application>(context)
        }
    }
}
