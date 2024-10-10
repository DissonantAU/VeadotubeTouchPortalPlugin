pluginManagement {

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "VeadotubeTouchPortalPlugin"

/* Library Substitution */
val enableLibrarySubstitution = true

if (enableLibrarySubstitution) {
    val useBleatkanLocal = true
    val useTouchPortalSdkLocal = false

    /* Get BleatKan Library from Local project - Should be in folder next to this Project */
    val inclBuildBleatkanDir = file("bleatkan")

    if (useBleatkanLocal && isDirectory(inclBuildBleatkanDir)) {
        includeBuild(inclBuildBleatkanDir) {
            dependencySubstitution {
                substitute(module("io.github.dissonantau:bleatkan"))
                    .using(project(":lib"))
            }
        }
    }


    /*
    * Optional: Get TouchPortal SDK from Local project - Should be in folder next to this Project
    * Useful if TP SDK has Libraries that need Updating, etc.
    */
    val inclBuildTouchPortalSdkDir = file("../TouchPortalPluginSDK-8.3.0")

    if (useTouchPortalSdkLocal && isDirectory(inclBuildTouchPortalSdkDir)) {
        includeBuild(inclBuildTouchPortalSdkDir) {
            dependencySubstitution {
                substitute(module("com.christophecvb.touchportal:plugin-sdk"))
                    .using(project(":Library"))

                substitute(module("com.christophecvb.touchportal:plugin-sdk-annotations-processor"))
                    .using(project(":AnnotationsProcessor"))
            }
        }
    }
}

fun isDirectory(buildFile:File) = try { buildFile.exists() && buildFile.isDirectory } catch (_: Exception) { false }

