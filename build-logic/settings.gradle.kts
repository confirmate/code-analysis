rootProject.name = "code-analysis"

dependencyResolutionManagement {
    // Reuse version catalog from the main build.
    versionCatalogs {
        create("libs", {
            from(files("../gradle/libs.versions.toml"))
        }
        )
    }
}