import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
    id("buildlogic.kotlin-application-conventions")
    alias(libs.plugins.ktor)
    alias(libs.plugins.node)
}

application { mainClass = "de.fraunhofer.aisec.confirmate.technicaldocument.ApplicationKt" }

dependencies {
    implementation(project(":app"))

    // Ktor server
    implementation(libs.bundles.ktor.server)

    implementation(libs.bundles.jackson)
    implementation(libs.ktor.serialization.jacksonjvm)
}

node {
    download.set(true)
    version.set(libs.versions.node)
    nodeProjectDir.set(file("${project.projectDir.resolve("src/main/webapp")}"))
}

val pnpmBuild by
    tasks.registering(PnpmTask::class) {
        inputs.file("src/main/webapp/package.json").withPathSensitivity(PathSensitivity.RELATIVE)
        inputs.file("src/main/webapp/pnpm-lock.yaml").withPathSensitivity(PathSensitivity.RELATIVE)
        inputs.dir("src/main/webapp/src").withPathSensitivity(PathSensitivity.RELATIVE)
        outputs.dir("src/main/resources/static")
        outputs.cacheIf { true }

        workingDir.set(file("src/main/webapp"))
        pnpmCommand.set(listOf("run", "build"))
        dependsOn(tasks.getByName("pnpmInstall"))
    }

tasks.processResources { dependsOn(pnpmBuild) }

var jarTasks = tasks.withType<Jar>()

jarTasks.forEach { it.dependsOn(pnpmBuild) }

tasks.shadowJar { setProperty("zip64", true) }
