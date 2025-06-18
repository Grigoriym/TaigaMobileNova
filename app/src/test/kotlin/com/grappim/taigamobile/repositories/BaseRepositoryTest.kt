package com.grappim.taigamobile.repositories

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grappim.taigamobile.data.api.TaigaApi
import com.grappim.taigamobile.manager.TaigaTestInstanceManager
import com.grappim.taigamobile.manager.UserInfo
import com.grappim.taigamobile.state.Session
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@RunWith(AndroidJUnit4::class)
abstract class BaseRepositoryTest {
    lateinit var mockSession: Session
    lateinit var mockTaigaApi: TaigaApi
    lateinit var activeUser: UserInfo

    private val taigaManager = TaigaTestInstanceManager()

    @BeforeTest
    fun setup() {
        taigaManager.setup()
        activeUser = taigaManager.activeUser

        val dataModule = DataModule() // contains methods for API configuration

        mockSession =
            Session(ApplicationProvider.getApplicationContext(), dataModule.provideMoshi()).also {
                it.changeServer(taigaManager.baseUrl)

                activeUser.data.apply {
                    it.changeCurrentUserId(id)
                    it.changeAuthCredentials(accessToken, refreshToken)
                }

                activeUser.projects.entries.first().let { (id, project) ->
                    it.changeCurrentProject(id, project.name)
                }
            }
        mockTaigaApi = dataModule.provideTaigaApi(mockSession, dataModule.provideMoshi())
    }

    @AfterTest
    fun cleanup() {
        taigaManager.clear()
    }
}
