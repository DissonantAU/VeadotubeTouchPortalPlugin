import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)

    `java-library`
}

group = "io.github.dissonantau"

/* Version */
val versionMajor: Int = 0
val versionMinor: Int = 6
val versionPatch: Int = 1 //Is padded with 0 to left if needed

val isRelease = System.getenv("IS_RELEASE") == "YES"
val versionSuffix: String = isRelease.ifFalse { "-DEV" }.orEmpty()

// Version becomes 1203
val versionCode: Int = versionMajor * 1000 + versionMinor * 100 + versionPatch
extra["versionCode"] = versionCode

// Version becomes 1.2.03 (Or 1.2.03-DEV etc.)
val versionName: String = "$versionMajor.$versionMinor.${versionPatch.toString().padStart(2, '0')}$versionSuffix"
extra["versionName"] = versionName
version = versionName


repositories {
    mavenCentral()
}

dependencies {
    /* Main Dependencies */
    //Kotlin BOM
    runtimeOnly(libs.kotlin.bom)
    implementation(platform(libs.kotlin.gradle.plugins.bom))

    // Coroutines - concurrent library
    implementation(libs.kotlinx.coroutines.bom)
    implementation(libs.kotlinx.coroutines.core)
    runtimeOnly(libs.kotlinx.coroutines.slf4j)

    // Websocket (and HTTP) Framework
    implementation(platform(libs.ktor.client.bom))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.logging)
    // HTTP Engines - pick one
    implementation(libs.ktor.client.cio) // No HTTP/2 Support, fine for Veadotube Websockets
    // JSON - probably best to use Probably KotlinX for JSON
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.serialization)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization.json)


    // Log4J
    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.core)
    implementation(libs.log4j.api)
    implementation(libs.log4j.slf4j2.impl)
    // Generic Logging Interface that can use Log4J
    implementation(platform(libs.slf4j.bom))
    implementation(libs.slf4j.api)
    implementation(libs.logging.kotlin) //Kotlin Wrapper for slf4j

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.debug)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test.junit5)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.ktor.client.mock)

    testImplementation(libs.mockk)

}

kotlin {
    //jvmToolchain(8)

    compilerOptions {
        javaParameters = true // Needed if Reflection is used to get named parameters
        jvmTarget.set(JvmTarget.JVM_1_8)
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

java {
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Jar> {
    archiveBaseName.set(rootProject.name)
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to rootProject.name,
                "Implementation-Version" to project.version
            )
        )
    }
}

tasks.test {
    useJUnitPlatform()
}