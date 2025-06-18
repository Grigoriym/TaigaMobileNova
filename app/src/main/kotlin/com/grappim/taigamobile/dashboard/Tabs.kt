package com.grappim.taigamobile.dashboard

import androidx.annotation.StringRes
import com.grappim.taigamobile.R
import com.grappim.taigamobile.ui.components.containers.Tab

enum class Tabs(@StringRes override val titleId: Int) : Tab {
    WorkingOn(R.string.working_on),
    Watching(R.string.watching),
    MyProjects(R.string.my_projects)
}
