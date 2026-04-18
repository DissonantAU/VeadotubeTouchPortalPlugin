import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)

    id("java")
    //id("application")

    alias(libs.plugins.gmazzo.buildconfig)
    alias(libs.plugins.touchportal.plugin.packager)
}

/* Versions follow Semantic Versioning (https://semver.org) for most things and a simple version number for inside Touch Portal
* <version>-<pre-release>+<metadata>
* Full = 1.1.2
* Pre-release = alpha.<build date>-<build time> or beta.<build date>
* Metadata = resources bundle (e.g. debug/trace)
* e.g. 1.0.4-alpha.20241103-1234+debug or 1.0.4-beta.20241205+debug
*/
val versionMajor: Int = 0
val versionMinor: Int = 8
val versionPatch: Int = 2


val pluginFullName: String = "Veadotube Touch Portal Plugin"
val pluginShortName: String = "Veadotube Plugin"

val pluginDownloadPage: String = "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/latest"

val mainClassSimpleName: String = "VeadoTouchPlugin"
val mainClassPackage: String = "io.github.dissonantau.veadotubetouchportalplugin"

group = mainClassPackage
tpPlugin.mainClassSimpleName.set(mainClassSimpleName)

// Java Version to Target - Java 8 is default. Normally set by Build Tasks
// Newer versions of TP use JRE 17 if you're able to use the included JVM
tpPlugin.targetJvmVersion.set(8)

/* Build Dirs */
val pluginsBuildsDir: Directory = rootProject.layout.projectDirectory.dir("pluginBuilds")
println("veadotube PluginBuilds Dir: $pluginsBuildsDir")

val resourcesMain: Directory = layout.projectDirectory.dir("src/main/resources")
val resourcesRelease: Directory = layout.projectDirectory.dir("src/release/resources")
val resourcesDebug: Directory = layout.projectDirectory.dir("src/debug/resources")
val resourcesTrace: Directory = layout.projectDirectory.dir("src/trace/resources")

/* Gradle defined run task */
//application.mainClass = "$mainClassPackage.$mainClassSimpleName"

//tasks.run<JavaExec> {
//    dependsOn(tasks.packagePlugin)
//
//    args(listOf("start"))
//    workingDir =
//        project.layout.buildDirectory.get().dir("plugin").dir(mainClassSimpleName).asFile
//}

val releaseName = mainClassSimpleName
println("Release project: $releaseName")

// Version becomes 1203
val versionCode: Int = versionMajor * 1000 + versionMinor * 100 + versionPatch
println("Version Code: $versionCode")

// Version Base Name becomes 1.2.3, doesn't change
val versionBaseName = "$versionMajor.$versionMinor.$versionPatch"
project.version = versionBaseName

// Version Semantic Name - becomes 1.2.3-alpha etc. - will be updated as needed
val versionSemanticProvider =
    objects.property(String::class).convention("$versionMajor.$versionMinor.$versionPatch-snapshot")

// If Build is Release - No Tags. Changed by tasks if needed
val buildIsRelease = objects.property(Boolean::class).convention(false)

// If Build should be flagged so TP launches using the Internal Changed by tasks if needed
val tpUseInternalJreProvider = objects.property(Boolean::class).convention(false)
val compilerAnnotationArguments: ListProperty<String> = objects.listProperty<String>()

// whether this is alpha/beta/snapshot. Ignored during Build Release. Set by Build Tasks
val buildPreReleaseTag = objects.property(String::class).convention("SNAPSHOT")

// Chooses which resources bundle to include in IDE Testing - e.g. debug/trace and metadata extension for non-release builds
// Normally set by Build Tasks
// Mainly for choosing logging options - "DEBUG" by default, change to "TRACE" if needed for IDE Run.
val buildResourcesBundle = objects.property(String::class).convention("TRACE")


updateReleaseType()
generateSemanticVersion(buildIsRelease, buildPreReleaseTag, buildResourcesBundle)
setMainResources(buildResourcesBundle)


buildConfig {
    packageName.set(project.group.toString())

    // Full Name - Shows in some logs - e.g "Veadotube Touch Portal Plugin"
    buildConfigField("String", "NAME", "\"$pluginFullName\"")
    // Short Name - What shows in Touch Portal - e.g. "Veadotube Plugin"
    buildConfigField("String", "NAME_SHORT", "\"$pluginShortName\"")

    // Version as Long - 1.7.11 > 1711
    buildConfigField("long", "VERSION_CODE", "$versionCode")
    // Version Base Name - e.g. "1.7.11"
    buildConfigField("String", "VERSION_NAME_BASE", "\"${versionBaseName}\"")
    // Version Full SemVer Name - including any extra types, etc. - e.g. "1.7.11-snapshot.20241228-2119+debug"
    buildConfigField("String", "VERSION_NAME_FULL", provider { "\"${versionSemanticProvider.get()}\"" })

    // Is Release Build - true/false
    buildConfigField("boolean", "BUILD_IS_RELEASE", buildIsRelease)

    // Use TP Bundled JRE - true/false
    buildConfigField("boolean", "USES_TP_BUNDLED_JRE", tpUseInternalJreProvider)

    // Pre Release Version - "ALPHA", "BETA", "SNAPSHOT", or blank
    buildConfigField("String", "BUILD_PRE_RELEASE_VERSION", provider { "\"${buildPreReleaseTag.get()}\"" })

    // Build Resources Bundle Value - "INFO", "TRACE", "DEBUG"
    buildConfigField("String", "BUILD_RESOURCES_BUNDLE", provider { "\"${buildResourcesBundle.get()}\"" })

    // Java Target Specification - Minimum Version Targeted by JAR
    buildConfigField("integer", "TARGET_JRE_SPEC", provider { tpPlugin.targetJvmVersion.get() })

    // Java JDK Specification - Major Version of the JDK this is Building the JAR
    buildConfigField("integer", "BUILD_JDK_SPEC", provider { JavaVersion.current().majorVersion })

    // URL to the Download Page
    buildConfigField("String", "PLUGIN_RELEASES_DOWNLOAD_PAGE", "\"$pluginDownloadPage\"")

    // URL to the JSON file that lists versions of the Plugin
    buildConfigField(
        "String",
        "PLUGIN_RELEASES_UPDATE_CHECK_URI",
        "\"https://dissonantau.github.io/veadoTouchPortalPlugin/releases.json\""
    )
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
    implementation(platform(libs.kotlin.bom))
    implementation(platform(libs.kotlin.gradle.plugins.bom))

    // Coroutines - concurrent library
    implementation(libs.kotlinx.coroutines.bom)
    implementation(libs.kotlinx.coroutines.core)
    runtimeOnly(libs.kotlinx.coroutines.slf4j)

    // HTTP/Websocket Framework
    implementation(platform(libs.ktor.client.bom))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.logging)
    // HTTP Engine
    implementation(libs.ktor.client.cio) // No HTTP/2 Support, fine for Veadotube Websockets
    // JSON - probably best to use Probably KotlinX for JSON
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.serialization)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization.json)


    // Apache Commons Lang 3 - mainly for SystemUtils
    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation(libs.apache.commons.lang3)

    // Apache Commons Collections 4 - mainly for LRUMap
    // https://mvnrepository.com/artifact/org.apache.commons/commons-collections4
    implementation(libs.apache.commons.collections4)

    // SemVer - for comparing versions in Update Checker
    // https://mvnrepository.com/artifact/net.swiftzer.semver/semver
    implementation(libs.semver)


    /* Logging Dependencies */
    // Log4J
    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.core)
    implementation(libs.log4j.api)
    implementation(libs.log4j.slf4j2.impl)
    // Generic Logging Interface that can use Log4J
    implementation(platform(libs.slf4j.bom))
    implementation(libs.slf4j.api)
    implementation(libs.logging.kotlin) //Kotlin Wrapper for slf4j


    /* Testing Dependencies */
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.debug)
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
        //jvmTarget.set(JvmTarget.JVM_1_8)
        //jvmTarget.set(JvmTarget.JVM_17)
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

java {
    //sourceCompatibility = JavaVersion.VERSION_1_8
    //targetCompatibility = JavaVersion.VERSION_1_8

    //sourceCompatibility = JavaVersion.VERSION_17
    //targetCompatibility = JavaVersion.VERSION_17
}


tasks {
    withType<Jar> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    // Calculate Version/Build names, etc.
    register("calculatePluginVersion") {
        doFirst {
            updateReleaseType()
            generateSemanticVersion(buildIsRelease, buildPreReleaseTag, buildResourcesBundle)
        }

        doLast {
            setMainResources(buildResourcesBundle)
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

    /* Task to copy to PluginBuilds after build */
    register<Copy>("copyToPluginBuilds") {
        duplicatesStrategy = DuplicatesStrategy.WARN

        mustRunAfter(
            named("calculatePluginVersion")
        )

        doFirst {
            println("Copy to '${pluginsBuildsDir.dir("${releaseName}_${versionBaseName}")}'")
        }

        dependsOn(
            named("calculatePluginVersion"),
            packagePlugin
        )
        from(packagePlugin)
        into { pluginsBuildsDir.dir("${releaseName}_${versionBaseName}") }
        rename { filename ->
            val newFilename =
                filename.replace(
                    ".tpp",
                    if (tpUseInternalJreProvider.get()) {
                        "_${versionSemanticProvider.get()}_internalJRE.tpp"
                    } else {
                        "_${versionSemanticProvider.get()}.tpp"
                    }
                )
            println("Copy $filename to $newFilename")
            newFilename
        }

    }

    /* Compiler Options */
    withType<JavaCompile>().forEach { thisTask ->
        thisTask.doFirst {
            // Get Annotation Arguments from Provider, add -A to start
            val javaCompilerArgs = thisTask.options.compilerArgs
            compilerAnnotationArguments.get().forEach { newArg ->
                println("Task ${thisTask.name}: add argument: '$newArg'")
                javaCompilerArgs.add("-A$newArg")
            }
        }
    }

    /* Compiler Options */
    withType<KotlinCompile>().forEach { thisTask ->
        thisTask.doFirst {
            // Get Annotation Arguments from Provider, add -A to start
            compilerAnnotationArguments.get().forEach { newArg ->
                println("Task ${thisTask.name}: add argument: '$newArg'")
                kapt.arguments { arg(newArg) }
            }
        }
    }

    withType<Jar>().named("jar") {
        dependsOn(
            named("calculatePluginVersion")
        )

        doFirst {
            println(
                "Setting JAR archive for ${rootProject.name} - BaseName = ${releaseName}; Version = $versionBaseName"
            )
        }

        // Set Names for build archive
        archiveBaseName.set(provider { releaseName })
        archiveVersion.set(provider { versionBaseName })
    }

    register("cleanBuildLibs") {
        doFirst {
            // Cleanup Libs folder
            val libs = project.layout.buildDirectory.get().dir("libs")
            println("Cleaning Build Libs: $libs")
            libs.asFileTree.files.forEach {
                println("Deleting ${it.name}")
                delete(it)
            }
        }
    }

    register("buildEnableTPInternalJRELaunch") {
        doFirst {
            // If tpUseInternalJreProvider is true, add needed argument for annotation processor
            if (tpUseInternalJreProvider.get()) {
                compilerAnnotationArguments.add("tp.entry.startcmd.jre.all.internal")
            }
        }
    }

    /* Meta Build Jobs */

    register("buildCopyBetaTraceToPluginBuilds") {
        group = "buildCopy"

        doFirst {
            println("Set to Beta Trace Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("TRACE")
            buildPreReleaseTag.set("BETA")
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyBetaTraceInternalToPluginBuilds") {
        group = "buildCopy"

        doFirst {
            println("Set to Beta Trace Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("TRACE")
            buildPreReleaseTag.set("BETA")
            tpUseInternalJreProvider.set(true)
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaTraceToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Alpha Trace Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("TRACE")
            buildPreReleaseTag.set("ALPHA")
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaTraceInternalToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Alpha Trace Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("TRACE")
            buildPreReleaseTag.set("ALPHA")
            tpUseInternalJreProvider.set(true)
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyBetaDebugToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Beta Debug Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("DEBUG")
            buildPreReleaseTag.set("BETA")
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyBetaDebugInternalToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Beta Debug Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("DEBUG")
            buildPreReleaseTag.set("BETA")
            tpUseInternalJreProvider.set(true)
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaDebugToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Alpha Debug Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("DEBUG")
            buildPreReleaseTag.set("ALPHA")
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaDebugInternalToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Alpha Debug Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("DEBUG")
            buildPreReleaseTag.set("ALPHA")
            tpUseInternalJreProvider.set(true)
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyBetaInfoToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Beta Debug Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("INFO")
            buildPreReleaseTag.set("BETA")
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyBetaInfoInternalToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Beta Debug Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("INFO")
            buildPreReleaseTag.set("BETA")
            tpUseInternalJreProvider.set(true)
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaInfoToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Alpha Debug Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("INFO")
            buildPreReleaseTag.set("ALPHA")
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyAlphaInfoInternalToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Alpha Debug Build")
            buildIsRelease.set(false)
            buildResourcesBundle.set("INFO")
            buildPreReleaseTag.set("ALPHA")
            tpUseInternalJreProvider.set(true)
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyReleaseToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Release Build")
            buildIsRelease.set(true)
            buildResourcesBundle.set("INFO")
            buildPreReleaseTag.set("")
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyReleaseInternalToPluginBuilds") {
        group = "build copy"

        doFirst {
            println("Set to Release Build")
            buildIsRelease.set(true)
            buildResourcesBundle.set("INFO")
            buildPreReleaseTag.set("")
            tpUseInternalJreProvider.set(true)
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
            named("copyToPluginBuilds"),
        )
    }

    register("buildCopyReleaseToPluginBuildsJVM17") {
        group = "build copy"

        doFirst {
            println("Set to Release Build,JVM17")
            buildIsRelease.set(true)
            buildResourcesBundle.set("INFO")
            buildPreReleaseTag.set("")
            tpPlugin.targetJvmVersion.set(17)
        }

        finalizedBy(
            //named("cleanBuildLibs"),
            named("buildEnableTPInternalJRELaunch"),
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
                    buildIsRelease.set(true)
                    buildResourcesBundle.set("INFO")
                }

                "TRACE" -> {
                    buildIsRelease.set(false)
                    buildResourcesBundle.set("TRACE")
                }

                else -> {
                    // DEBUG or TRACE
                    buildIsRelease.set(false)
                    buildResourcesBundle.set("DEBUG")
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
        if (releaseBuildProvider.get()) versionBaseName
        else {
            println("Base Version Name: $versionBaseName")

            val preReleaseVersion = preReleaseVersionProvider.get()
            println("Pre-release Version: $preReleaseVersion")
            val pattern = when (preReleaseVersion) {
                "BETA" -> "yyyyMMdd"
                else -> "yyyyMMdd-HHmm"
            }

            val timeOfBuild = DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now())
            println("Time of Build: $timeOfBuild")

            val resourcesBundle = resourcesBundleProvider.get()
            val metadata = if (resourcesBundle.isNotBlank()) {
                if (resourcesBundle.isNotBlank()) println("Resources Metadata: $resourcesBundle")
                "+${resourcesBundle.lowercase()}"
            } else ""


            "$versionMajor.$versionMinor.$versionPatch-${preReleaseVersion.lowercase()}.${timeOfBuild}$metadata"
        }

    versionSemanticProvider.set(genVersionSemantic)
    project.version = genVersionSemantic
    println("Version Updated Name: $genVersionSemantic")
}


fun setMainResources(release: Provider<String>) {
    val releaseType = release.get()
    println("Assign Source Set Resources - currently $releaseType")

    sourceSets.main.get().resources.setSrcDirs(listOf(resourcesMain))
    sourceSets.main {
        resources {
            when (releaseType) {
                "INFO" -> srcDir(resourcesRelease)
                "TRACE" -> srcDir(resourcesTrace)
                "DEBUG" -> srcDir(resourcesDebug)
                else -> throw IllegalArgumentException("Missing Resources Tag")
            }
        }
    }
}