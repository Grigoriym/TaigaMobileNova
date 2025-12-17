plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
}

dependencies {
    implementation(projects.core.domain)

    implementation(projects.feature.workitem.domain)
}
