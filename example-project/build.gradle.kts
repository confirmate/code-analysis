plugins { id("buildlogic.kotlin-application-conventions") }

dependencies {
    api(project(":app"))
    api(project(":queries"))
}

application { mainClass = "de.fraunhofer.aisec.example.MainKt" }

tasks.named<JavaExec>("run") { workingDir = rootProject.projectDir }
