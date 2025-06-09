package io.eugenethedev.taigamobile.core.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.eugenethedev.taigamobile.R

enum class Screens(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val iconId: Int
) {
    Dashboard(Routes.dashboard, R.string.dashboard_short, R.drawable.ic_dashboard),
    Scrum(Routes.scrum, R.string.scrum, R.drawable.ic_scrum),
    Epics(Routes.epics, R.string.epics, R.drawable.ic_epics),
    Issues(Routes.issues, R.string.issues, R.drawable.ic_issues),
    More(Routes.more, R.string.more, R.drawable.ic_more)
}
