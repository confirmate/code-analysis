import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("buildlogic.kotlin-common-conventions")

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

val libs = the<LibrariesForLibs>()  // necessary to be able to use the version catalog in buildSrc
dependencies {
    // Unit tests
    testImplementation(kotlin("test"))
}
