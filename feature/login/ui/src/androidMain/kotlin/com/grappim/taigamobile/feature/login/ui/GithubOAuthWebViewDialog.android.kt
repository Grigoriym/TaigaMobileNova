package com.grappim.taigamobile.feature.login.ui

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
actual fun GithubOAuthWebViewDialog(
    url: String,
    onCodeReceived: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            val code = request.url.getQueryParameter("code")
                            if (code != null) {
                                onCodeReceived(code)
                                return true
                            }
                            if (request.url.getQueryParameter("error") != null) {
                                onDismiss()
                                return true
                            }
                            return false
                        }
                    }
                    loadUrl(url)
                }
            }
        )
    }
}
