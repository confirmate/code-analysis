plugins { id("buildlogic.kotlin-application-conventions") }

dependencies {
    api(project(":app"))
    api(project(":queries"))
}

application { mainClass = "example.MainKt" }
