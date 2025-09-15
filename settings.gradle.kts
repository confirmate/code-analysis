rootProject.name = "code-analysis"

pluginManagement {
    // Include 'plugins build' to define convention plugins.
    includeBuild("build-logic")
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("app", "queries")

include(":orchestrator")
//project(":generator_orchestrator").projectDir = rootDir.resolve("generated/generator_orchestrator")

include(":evidence")
//project(":evidence").projectDir = rootDir.resolve("build/evidence")
