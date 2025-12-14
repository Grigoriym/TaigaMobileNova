package com.grappim.taigamobile.feature.dashboard.ui

import androidx.annotation.StringRes
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.Tab

enum class DashboardTabs(@StringRes override val titleId: Int) : Tab {
    WorkingOn(RString.working_on),
    Watching(RString.watching)
}
