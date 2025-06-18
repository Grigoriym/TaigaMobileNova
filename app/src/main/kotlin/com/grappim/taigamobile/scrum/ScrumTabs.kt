package com.grappim.taigamobile.scrum

import androidx.annotation.StringRes
import com.grappim.taigamobile.R
import com.grappim.taigamobile.ui.components.containers.Tab

enum class ScrumTabs(@StringRes override val titleId: Int) : Tab {
    Backlog(R.string.backlog),
    Sprints(R.string.sprints_title)
}
