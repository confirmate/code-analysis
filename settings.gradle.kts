pluginManagement {
    // Include 'plugins build' to define convention plugins.
    includeBuild("build-logic")
}

plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.3"
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "code-analysis"

include("app")

include("queries")

include("example-project")

kover {
    enableCoverage()
}