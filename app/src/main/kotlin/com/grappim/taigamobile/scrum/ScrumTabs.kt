package com.grappim.taigamobile.scrum

import androidx.annotation.StringRes
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.Tab

enum class ScrumTabs(@StringRes override val titleId: Int) : Tab {
    Backlog(RString.backlog),
    Sprints(RString.sprints_title)
}
