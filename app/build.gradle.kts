plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    implementation("dev.kord:kord-core:0.17.0")
    implementation("dev.kord:kord-core-voice:0.17.0")
    implementation("dev.kord:kord-voice:0.17.0")
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "fr.outadoc.bruitage.app.AppKt"
}
