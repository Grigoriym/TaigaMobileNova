package com.grappim.taigamobile.feature.login.ui

import androidx.compose.runtime.Composable

@Composable
expect fun GithubOAuthWebViewDialog(url: String, onCodeReceive: (String) -> Unit, onDismiss: () -> Unit)

expect fun isGithubOAuthSupported(): Boolean
