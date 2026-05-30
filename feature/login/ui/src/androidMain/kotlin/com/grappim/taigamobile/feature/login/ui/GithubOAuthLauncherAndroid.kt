package com.grappim.taigamobile.feature.login.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.grappim.taigamobile.feature.login.domain.launcher.GithubOAuthLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.net.ServerSocket
import java.net.SocketException

@Single(binds = [GithubOAuthLauncher::class])
class GithubOAuthLauncherAndroid(private val context: Context) : GithubOAuthLauncher {

    override suspend fun launch(baseAuthUrl: String): String {
        val serverSocket = ServerSocket(0)
        val port = serverSocket.localPort

        val redirectUri = "http://127.0.0.1:$port/callback"
        val fullUrl = "$baseAuthUrl&redirect_uri=${Uri.encode(redirectUri)}"

        val tabIntent = CustomTabsIntent.Builder().build()
        tabIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        tabIntent.launchUrl(context, Uri.parse(fullUrl))

        return withContext(Dispatchers.IO) {
            currentCoroutineContext()[Job]?.invokeOnCompletion { serverSocket.close() }
            try {
                serverSocket.soTimeout = TIMEOUT_MS
                val clientSocket = serverSocket.accept()
                val requestLine = clientSocket.getInputStream().bufferedReader().readLine().orEmpty()
                clientSocket.getOutputStream().write(CLOSE_PAGE_HTML.toByteArray())
                clientSocket.close()
                bringToForeground()

                requestLine
                    .substringAfter("code=", "")
                    .substringBefore("&")
                    .substringBefore(" ")
                    .also { require(it.isNotBlank()) { "No OAuth code in GitHub callback" } }
            } catch (e: SocketException) {
                currentCoroutineContext().ensureActive()
                throw e
            } finally {
                runCatching { serverSocket.close() }
            }
        }
    }

    private fun bringToForeground() {
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            context.startActivity(intent)
        }
    }

    companion object {
        private const val TIMEOUT_MS = 5 * 60 * 1000
        private const val CLOSE_PAGE_HTML =
            "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" +
                "<html><body>Authentication complete. Return to the app.</body></html>"
    }
}
