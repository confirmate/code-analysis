import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.diffplug.spotless")
}

tasks.withType<KotlinCompile> {
    dependsOn("spotlessApply")
}

val headerWithStars = """/*
 * This file is part of the Confirmate project.
 */
"""

spotless {
    kotlin {
        target("**/*.kt", "**/*.codyze.kts")
        ktfmt("0.55").kotlinlangStyle()
        licenseHeader(headerWithStars).yearSeparator(" - ")
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle()
    }
}