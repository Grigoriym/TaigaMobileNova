plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.room.ktx)
            implementation(libs.androidx.datastore.android)
        }
        commonMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.androidx.sqlite.bundled)

            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)
            implementation(projects.core.appinfoApi)
            implementation(projects.utils.ui)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.projects.domain)

            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.paging.common)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
