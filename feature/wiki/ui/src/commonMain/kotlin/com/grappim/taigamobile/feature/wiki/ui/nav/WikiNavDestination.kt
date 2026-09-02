package com.grappim.taigamobile.feature.wiki.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object WikiNavDestination : NavKey

@Serializable
data object WikiPagesNavDestination : NavKey

@Serializable
data object WikiLinksNavDestination : NavKey

@Serializable
data object WikiCreatePageNavDestination : NavKey

@Serializable
data object WikiCreateLinkNavDestination : NavKey
