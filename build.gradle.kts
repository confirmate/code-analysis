plugins {
    id("test-report-aggregation")
}

// this is needed for the plugins block
repositories {
    mavenCentral()
}

allprojects {
    group = "de.fraunhofer.aisec"
}
