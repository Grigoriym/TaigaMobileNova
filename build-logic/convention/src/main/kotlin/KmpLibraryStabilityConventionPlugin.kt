import com.grappim.taigamobile.buildlogic.configureComposeStabilityMarker
import com.grappim.taigamobile.buildlogic.configureComposeStabilityReports
import org.gradle.api.Plugin
import org.gradle.api.Project

// Applied alongside `taigamobile.kmp.library` on `*/domain` modules whose types are consumed as
// Composable parameters elsewhere — see docs/revisit.md #39 and docs/compose/stability-reports.md.
class KmpLibraryStabilityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureComposeStabilityMarker()
            configureComposeStabilityReports()
        }
    }
}
