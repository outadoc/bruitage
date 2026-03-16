plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.lavakord.kord)
    implementation(libs.lavakord.plugins.sponsorblock)
    implementation(libs.kord.core)
    implementation(libs.kord.core.voice)
    implementation(libs.kord.voice)
    implementation(libs.kotlinx.io.core)
    implementation(libs.langchain.core)
    implementation(libs.langchain.mistral)
    implementation(libs.slf4j)
}

application {
    mainClass = "fr.outadoc.bruitage.app.AppKt"
}
