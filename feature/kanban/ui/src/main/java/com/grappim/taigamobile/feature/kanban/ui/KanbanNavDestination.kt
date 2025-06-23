
import androidx.navigation.NavController
import com.grappim.taigamobile.core.navigation.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object KanbanNavDestination

fun NavController.navigateToKanbanAsTopDestination() {
    navigate(route = KanbanNavDestination) {
        popUpToTop(this@navigateToKanbanAsTopDestination)
    }
}
