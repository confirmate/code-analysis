plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("buildlogic.kotlin-common-conventions")
    id("buildlogic.kotlin-formatting-conventions")

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}
