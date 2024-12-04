import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)

    id("java")
    id("application")

    alias(libs.plugins.gmazzo.buildconfig)
    alias(libs.plugins.touchportal.plugin.packager)
}

/* Version */
val versionMajor: Int = 0
val versionMinor: Int = 6
val versionPatch: Int = 88 //Is padded with 0 to left if needed


val pluginFullName: String = "Veadotube Touch Portal Plugin"
val pluginShortName: String = "Veadotube Plugin"

val mainClassSimpleName: String = "VeadoTouchPlugin"
val mainClassPackage: String = "io.github.dissonantau.veadotubetouchportalplugin"
group = mainClassPackage

tpPlugin.mainClassSimpleName.set(mainClassSimpleName)

/* Gradle defined run task */
application.mainClass = "$mainClassPackage.$mainClassSimpleName"

tasks.run<JavaExec> {
    dependsOn(tasks.packagePlugin)

    args(listOf("start"))
    workingDir =
        project.layout.buildDirectory.get().dir("plugin").dir(mainClassSimpleName).asFile
}


val buildsDir: Directory = rootProject.layout.projectDirectory.dir("pluginBuilds")
println("veadotube PluginBuilds Dir: $buildsDir")

val resourcesRelease: Directory = layout.projectDirectory.dir("src/release/resources")
val resourcesDebug: Directory = layout.projectDirectory.dir("src/debug/resources")
val resourcesTrace: Directory = layout.projectDirectory.dir("src/trace/resources")




project.extra["releaseName"] = mainClassSimpleName
println("Release project: ${project.extra["releaseName"]}")

// Version becomes 1203
val versionCode: Int = versionMajor * 1000 + versionMinor * 100 + versionPatch
project.extra["versionCode"] = versionCode
println("Version Code: $versionCode")

// Version Base Name becomes 1.2.03
val versionName =
    "$versionMajor.$versionMinor.${versionPatch.toString().padStart(2, '0')}"
project.extra["versionBaseName"] = versionName
project.extra["versionName"] = versionName
println("Version: $versionName")
project.version = versionName


var releaseTypeProvider: Provider<String> = provider { "DEV" }

calcVersion()
setMainResources(releaseTypeProvider)

buildConfig {
    packageName.set(project.group.toString())

    buildConfigField("String", "NAME", "\"$pluginFullName\"")
    buildConfigField("String", "NAME_SHORT", "\"$pluginShortName\"")
    buildConfigField("String", "VERSION_BASE_NAME", "\"${project.extra["versionBaseName"]}\"")
    buildConfigField("String", "VERSION_NAME", provider { "\"${project.extra["versionName"]}\"" })
    buildConfigField("long", "VERSION_CODE", "${project.extra["versionCode"]}")

    println(buildConfigFields)
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
    //jvmToolchain(8)

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

tasks {

    // Calculate Version/Build names, etc.
    register("calculatePluginVersion") {
        doFirst {
            calcVersion()
        }

        doLast {
            setMainResources(releaseTypeProvider)
        }
    }


    // Run Version Calculation during Build, including IDE Import/refresh
    // targeting BuildConfigTask - might not be needed anymore?
    //project.tasks.withType(BuildConfigTask::class).forEach {
    //    println("Add calculatePluginVersion to dependsOn $name > Class ${javaClass.name}")
    //    it.dependsOn(
    //        named("calculateVersion")
    //    )
    //}


    named<Copy>("processResources") {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    /* Task to build the project, copy to PluginBuilds*/

    register<Copy>("copyToPluginBuilds") {
        duplicatesStrategy = DuplicatesStrategy.WARN

        mustRunAfter(
            named("calculatePluginVersion")
        )

        doFirst {
            println("Copy to '${buildsDir.dir("${project.extra["releaseName"]}_${project.extra["versionBaseName"]}")}'")
        }

        dependsOn(packagePlugin)
        from(packagePlugin)
        into { buildsDir.dir("${project.extra["releaseName"]}_${project.extra["versionBaseName"]}") }
        rename { filename ->
            val newFilename = filename.replace(".tpp", "_${project.extra["versionName"]}.tpp")
            println("Copy $filename to $newFilename")
            newFilename
        }

    }

    /* Meta Build Jobs */

    register("buildCopyDevTraceToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Dev Trace Build")
            project.ext["BUILD_TYPE"] = "TRACE"
        }

        finalizedBy(
            named("calculatePluginVersion"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyDevToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Dev Debug Build")
            project.ext["BUILD_TYPE"] = "DEV"
        }

        finalizedBy(
            named("calculatePluginVersion"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyReleaseToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Release Build")
            project.ext["BUILD_TYPE"] = "RELEASE"
        }

        finalizedBy(
            named("calculatePluginVersion"),
            named("copyToPluginBuilds"),
        )
    }


    test {
        useJUnitPlatform()
    }
}




fun calcVersion() {
    println("Release project: ${project.extra["releaseName"]}")
    // Get Environment Var if it exists
    val envBuildType: String = try {
        System.getenv("BUILD_TYPE")
    } catch (_: Throwable) {
        ""
    }

    // Release Type - Variable from Build Tasks > Environment Var > Debug Default
    val releaseType: String =
        when {
            project.extra.has("BUILD_TYPE") -> "${project.extra["BUILD_TYPE"]}"
            envBuildType.isNotBlank() -> System.getenv("BUILD_TYPE")
            releaseTypeProvider.isPresent -> releaseTypeProvider.get()
            else -> "DEV"
        }

    println("Release Type: $releaseType")

    val buildSuffix: String = when (releaseType) {
        "RELEASE" -> ""
        "TRACE" -> "-DEV-TRACE"
        "DEV" -> "-DEV"
        else -> "-DEV"
    }

    // Version Name becomes 1.2.03 or 1.2.03-DEV etc.
    val versionNameSuffix = "${project.extra["versionBaseName"]}$buildSuffix"

    project.extra["versionName"] = versionNameSuffix
    project.version = versionNameSuffix
    println("Version Full Name: $versionNameSuffix")

    releaseTypeProvider = provider { releaseType }

}


fun setMainResources(release: Provider<String>) {
    println("Setting Source Set Resources to ${release.get()}")
    sourceSets.main {
        resources {
            when (release.get()) {
                "RELEASE" -> srcDir(resourcesRelease)
                "TRACE" -> srcDir(resourcesTrace)
                "DEV" -> srcDir(resourcesDebug)
                else -> srcDir(resourcesTrace)
            }
        }
    }
}