package com.grappim.taigamobile.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ProductFlavor
import org.gradle.kotlin.dsl.invoke

enum class FlavorDimensions {
    STORE
}

enum class AppFlavors(
    val title: String,
    val dimensions: FlavorDimensions,
    val applicationIdSuffix: String? = null
) {
    GPLAY("gplay", FlavorDimensions.STORE),
    FDROID("fdroid", FlavorDimensions.STORE, ".fdroid")
}

internal fun configureFlavors(
    commonExtension: CommonExtension,
    flavorConfigurationBlock: ProductFlavor.(flavor: AppFlavors) -> Unit = {}
) {
    commonExtension.apply {

        FlavorDimensions.entries.forEach { flavorDimension ->
            flavorDimensions += flavorDimension.name
        }

        productFlavors {
            AppFlavors.entries.forEach { flavor ->
                register(flavor.title) {
                    dimension = flavor.dimensions.name
                    flavorConfigurationBlock(this, flavor)
                    if (commonExtension is ApplicationExtension && this is ApplicationProductFlavor) {
                        if (flavor.applicationIdSuffix != null) {
                            applicationIdSuffix = flavor.applicationIdSuffix
                        }
                    }
                }
            }
        }
    }
}
