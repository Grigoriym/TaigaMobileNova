pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "TaigaMobileNova"

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    TaigaMobile requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}

include(":app")
include(":feature:login:domain")
include(":uikit")
include(":utils:ui")
include(":testing")
include(":feature:login:ui")
include(":feature:login:data")
include(":core:api")
include(":core:storage")
include(":core:domain")
include(":core:async")
include(":core:async-android")
include(":core:appinfo-api")
include(":feature:dashboard:ui")
include(":feature:dashboard:data")
include(":feature:dashboard:domain")
include(":feature:projects:data")
include(":feature:projects:domain")
include(":feature:wiki:data")
include(":feature:wiki:domain")
include(":feature:wiki:ui")
include(":feature:epics:data")
include(":feature:epics:domain")
include(":feature:epics:ui")
include(":feature:issues:data")
include(":feature:issues:ui")
include(":feature:issues:domain")
include(":strings")
include(":feature:sprint:data")
include(":feature:sprint:ui")
include(":feature:sprint:domain")
include(":feature:userstories:data")
include(":feature:userstories:ui")
include(":feature:userstories:domain")
include(":feature:settings:ui")
include(":feature:users:data")
include(":feature:users:domain")
include(":feature:kanban:ui")
include(":core:navigation")
include(":feature:tasks:data")
include(":feature:scrum:ui")
include(":feature:profile:ui")
include(":feature:projectselector:ui")
include(":feature:teams:ui")
include(":feature:filters:data")
include(":feature:swimlanes:data")
include(":feature:tasks:domain")
include(":feature:filters:domain")
include(":feature:history:data")
include(":feature:history:domain")
include(":feature:swimlanes:domain")
include(":feature:kanban:domain")
include(":feature:kanban:data")
include(":utils:formatter:decimal")
include(":feature:workitem:ui")
include(":feature:workitem:domain")
include(":utils:formatter:datetime")
