package io.eugenethedev.taigamobile.core.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.dashboard.DashboardNavDestination
import io.eugenethedev.taigamobile.ui.screens.epics.EpicsNavDestination
import io.eugenethedev.taigamobile.ui.screens.issues.IssuesNavDestination
import io.eugenethedev.taigamobile.ui.screens.more.MoreNavDestination
import io.eugenethedev.taigamobile.ui.screens.scrum.ScrumNavDestination
import kotlin.reflect.KClass

enum class Screens(
    val route: KClass<*>,
    @StringRes val resourceId: Int,
    @DrawableRes val iconId: Int
) {
    Dashboard(DashboardNavDestination::class, R.string.dashboard_short, R.drawable.ic_dashboard),
    Scrum(ScrumNavDestination::class, R.string.scrum, R.drawable.ic_scrum),
    Epics(EpicsNavDestination::class, R.string.epics, R.drawable.ic_epics),
    Issues(IssuesNavDestination::class, R.string.issues, R.drawable.ic_issues),
    More(MoreNavDestination::class, R.string.more, R.drawable.ic_more)
}
