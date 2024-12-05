import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)

    id("java")
    id("application")

    alias(libs.plugins.gmazzo.buildconfig)
    alias(libs.plugins.touchportal.plugin.packager)
}

/* Versions follow Semantic Versioning (https://semver.org) for most things and a simple version number for inside Touch Portal
* <version>-<pre-release>+<metadata>
* Full = 1.1.2
* Pre-release = alpha.<build date>-<build time> or beta.<build date>
* Metadata = resources bundle (eg debug/trace)
* eg. 1.0.4-alpha.20241103-1234+debug or 1.0.4-beta.20241205+debug
*/
/* Version */
val versionMajor: Int = 0
val versionMinor: Int = 6
val versionPatch: Int = 88


// Chooses which resources bundle to include in IDE Testing - e.g. debug/trace and metadata extension for non-release builds
// Mainly for choosing logging options - "DEBUG" by default, change to "TRACE" needed
project.extra["resourcesBundle"] = "DEBUG"



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

val resourcesMain: Directory = layout.projectDirectory.dir("src/main/resources")
val resourcesRelease: Directory = layout.projectDirectory.dir("src/release/resources")
val resourcesDebug: Directory = layout.projectDirectory.dir("src/debug/resources")
val resourcesTrace: Directory = layout.projectDirectory.dir("src/trace/resources")


project.extra["releaseName"] = mainClassSimpleName
println("Release project: ${project.extra["releaseName"]}")

// Version becomes 1203
val versionCode: Int = versionMajor * 1000 + versionMinor * 100 + versionPatch
project.extra["versionCode"] = versionCode
println("Version Code: $versionCode")

// Version Base Name becomes 1.2.3, doesn't change
val versionBaseName = "$versionMajor.$versionMinor.$versionPatch"
project.extra["versionBaseName"] = versionBaseName
project.version = versionBaseName

// Version Semantic Name - becomes 1.2.3-alpha etc. - will be updated as needed
project.extra["versionName"] = "$versionMajor.$versionMinor.$versionPatch-snapshot"
val versionSemanticProvider: Provider<String> = provider { "${project.extra["versionName"]}" }


// Changed by tasks if needed
project.extra["releaseBuild"] = false
val releaseBuildProvider: Provider<Boolean> = provider { project.extra["releaseBuild"] as Boolean }


// whether this is alpha/beta. Ignored during Build Release
project.extra["preReleaseVersion"] = "snapshot"
val preReleaseVersionProvider: Provider<String> = provider { "${project.extra["preReleaseVersion"]}" }

val resourcesBundleProvider: Provider<String> = provider { "${project.extra["resourcesBundle"]}" }


updateReleaseType()
generateSemanticVersion(releaseBuildProvider, preReleaseVersionProvider, resourcesBundleProvider)
setMainResources(resourcesBundleProvider)


buildConfig {
    packageName.set(project.group.toString())

    buildConfigField("String", "NAME", "\"$pluginFullName\"")
    buildConfigField("String", "NAME_SHORT", "\"$pluginShortName\"")
    buildConfigField("String", "VERSION_BASE_NAME", "\"${project.extra["versionBaseName"]}\"")
    buildConfigField("String", "VERSION_NAME", provider { "\"${project.extra["versionName"]}\"" })
    buildConfigField("long", "VERSION_CODE", "${project.extra["versionCode"]}")
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
            updateReleaseType()
            generateSemanticVersion(releaseBuildProvider, preReleaseVersionProvider, resourcesBundleProvider)
        }

        doLast {
            setMainResources(resourcesBundleProvider)
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

    register("buildCopyBetaTraceToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Beta Trace Build")
            project.extra["releaseBuild"] = false
            project.extra["resourcesBundle"] = "TRACE"
            project.extra["preReleaseVersion"] = "beta"
        }

        finalizedBy(
            named("calculatePluginVersion"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaTraceToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Alpha Trace Build")
            project.extra["releaseBuild"] = false
            project.extra["resourcesBundle"] = "TRACE"
            project.extra["preReleaseVersion"] = "alpha"
        }

        finalizedBy(
            named("calculatePluginVersion"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyBetaDebugToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Beta Debug Build")
            project.extra["releaseBuild"] = false
            project.extra["resourcesBundle"] = "DEBUG"
            project.extra["preReleaseVersion"] = "beta"
        }

        finalizedBy(
            named("calculatePluginVersion"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaDebugToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Alpha Debug Build")
            project.extra["releaseBuild"] = false
            project.extra["resourcesBundle"] = "DEBUG"
            project.extra["preReleaseVersion"] = "alpha"
        }

        finalizedBy(
            named("calculatePluginVersion"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyBetaInfoToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Beta Debug Build")
            project.extra["releaseBuild"] = false
            project.extra["resourcesBundle"] = "INFO"
            project.extra["preReleaseVersion"] = "beta"
        }

        finalizedBy(
            named("calculatePluginVersion"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaInfoToPluginBuilds") {
        group = "build"

        doFirst {
            println("Set to Alpha Debug Build")
            project.extra["releaseBuild"] = false
            project.extra["resourcesBundle"] = "INFO"
            project.extra["preReleaseVersion"] = "alpha"
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
            project.extra["releaseBuild"] = true
            project.extra["resourcesBundle"] = "INFO"
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



fun updateReleaseType() {

    // Get Environment Var if it exists
    try {
        val envBuildType: String = System.getenv("BUILD_TYPE")

        // Release Type - Variable from Build Tasks > Environment Var > Debug Default
        if (envBuildType.isNotBlank()) {
            println("Release Type: $envBuildType")

            when (envBuildType) {
                "RELEASE" -> {
                    project.extra["releaseBuild"] = true
                    project.extra["resourcesBundle"] = "INFO"
                }

                "TRACE" -> {
                    project.extra["releaseBuild"] = false
                    project.extra["resourcesBundle"] = "TRACE"
                }

                else -> {
                    project.extra["releaseBuild"] = false
                    project.extra["resourcesBundle"] = "DEBUG"
                }
            }
        }
    } catch (_: Throwable) {
    }

}


fun generateSemanticVersion(
    releaseBuildProvider: Provider<Boolean>,
    preReleaseVersionProvider: Provider<String>,
    resourcesBundleProvider: Provider<String>
) {
    val genVersionSemantic =
        if (releaseBuildProvider.get()) {
            "${project.extra["versionBaseName"]}"
        } else {
            println("Base Version Name: ${project.extra["versionBaseName"]}")
            val preReleaseVersion = preReleaseVersionProvider.get()
            println("Pre-release Version: $preReleaseVersion")
            val pattern = when (preReleaseVersion){
                "beta" -> "yyyyMMdd"
                else -> "yyyyMMdd-HHmm"
            }
            val timeOfBuild = DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now())
            println("Time of Build: $timeOfBuild")

            val metadata = if (resourcesBundleProvider.get().isNotBlank()) {
                val recMetaData = resourcesBundleProvider.get()
                if (recMetaData.isNotBlank()) println("Resources Metadata: $recMetaData")
                "+${recMetaData}".lowercase()
            } else {
                ""
            }

            "$versionMajor.$versionMinor.$versionPatch-$preReleaseVersion.${timeOfBuild}$metadata"
        }

    project.extra["versionName"] = genVersionSemantic
    project.version = genVersionSemantic
    println("Version Updated Name: $genVersionSemantic")
}


fun setMainResources(release: Provider<String>) {
    println("Assign Source Set Resources - currently ${release.get()}")
    sourceSets.main.get().resources.setSrcDirs(listOf(resourcesMain))
    sourceSets.main {
        resources {
            when (release.get()) {
                "INFO" -> srcDir(resourcesRelease)
                "TRACE" -> srcDir(resourcesTrace)
                "DEBUG" -> srcDir(resourcesDebug)
                else -> srcDir(resourcesTrace)
            }
        }
    }
}