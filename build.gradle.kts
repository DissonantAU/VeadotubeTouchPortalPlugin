import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)

    id("java")
    alias(libs.plugins.gmazzo.buildconfig)
    alias(libs.plugins.touchportal.plugin.packager)
}

val pluginFullName: String = "Veadotube Touch Portal Plugin"

val mainClassSimpleName: String = "VeadoTouchPlugin"
val mainClassPackage: String = "io.github.dissonantau.veadotubetouchportalplugin"
group = mainClassPackage

tpPlugin.mainClassSimpleName.set(mainClassSimpleName)

/* Version */
val versionMajor: Int = 0
val versionMinor: Int = 6
val versionPatch: Int = 0 //Is padded with 0 to left if needed

val isRelease = System.getenv("IS_RELEASE") == "YES"
val versionSuffix: String = isRelease.ifFalse { "-DEV" }.orEmpty()

// Version becomes 1203
val versionCode: Int = versionMajor * 1000 + versionMinor * 100 + versionPatch
extra["versionCode"] = versionCode

// Version becomes 1.2.03
val versionName: String = "$versionMajor.$versionMinor.${versionPatch.toString().padStart(2, '0')}$versionSuffix"
extra["versionName"] = versionName
version = versionName


buildConfig {
    packageName.set(project.group.toString())

    buildConfigField("String", "NAME", "\"$pluginFullName\"")
    buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
    buildConfigField("long", "VERSION_CODE", "$versionCode")
}

repositories {
    mavenCentral()
}

dependencies {
    /* Main Dependencies */
    // BleatKan
    implementation(libs.bleatkan)

    // Touch Portal
    implementation(libs.touchportal.plugin.sdk)
    kapt(libs.touchportal.plugin.sdk.processor)

    // Kotlin BOM
    runtimeOnly(libs.kotlin.bom)
    implementation(platform(libs.kotlin.gradle.plugins.bom))

    // Coroutines - concurrent library
    //implementation(libs.kotlinx.coroutines.bom)
    //implementation(libs.kotlinx.coroutines.core)
    //runtimeOnly(libs.kotlinx.coroutines.slf4j)

    // Log4J
    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.core)
    implementation(libs.log4j.api)
    implementation(libs.log4j.slf4j2.impl)
    // Generic Logging Interface that can use Log4J
    implementation(platform(libs.slf4j.bom))
    implementation(libs.slf4j.api)
    implementation(libs.logging.kotlin) //Kotlin Wrapper for slf4j

    // Apache Commons Collections 4 - mainly for LRUMap
    // https://mvnrepository.com/artifact/org.apache.commons/commons-collections4
    implementation(libs.apache.commons.collections4)


    /* Testing Dependencies */
    testImplementation(libs.kotlin.test)
    //testImplementation(libs.kotlinx.coroutines.debug)
    testImplementation(libs.kotlin.test.junit5)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.mockk)
}

kotlin {
    jvmToolchain(8)

    compilerOptions {
        javaParameters = true // Needed for TP SDK to get annotated parameter names correctly
        jvmTarget.set(JvmTarget.JVM_1_8)
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.test {
    useJUnitPlatform()
}