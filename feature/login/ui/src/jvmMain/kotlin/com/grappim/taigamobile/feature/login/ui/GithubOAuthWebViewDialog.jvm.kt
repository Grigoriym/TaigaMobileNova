package com.grappim.taigamobile.feature.login.ui

import androidx.compose.runtime.Composable

@Composable
actual fun GithubOAuthWebViewDialog(url: String, onCodeReceive: (String) -> Unit, onDismiss: () -> Unit) = Unit

actual fun isGithubOAuthSupported(): Boolean = false
