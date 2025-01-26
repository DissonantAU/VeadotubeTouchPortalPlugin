package io.github.dissonantau.veadotubetouchportalplugin

import io.github.dissonantau.veadotubetouchportalplugin.updatechecker.ReleaseData
import io.github.dissonantau.veadotubetouchportalplugin.updatechecker.UpdateCheckResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*

class VeadoTouchPluginTest {

    companion object {

        /** Class Logger */
        @JvmStatic
        private val LOGGER =
            KotlinLogging.logger { VeadoTouchPluginTest::class.java.name }

        private lateinit var testUpdateCheckResult: UpdateCheckResult

        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            testUpdateCheckResult = jsonDeserializer.decodeFromString(testUpdateJSON)
        }

        @JvmStatic
        @AfterAll
        fun tearDown(): Unit {
        }




        /**
         * JSON De/serializer
         */
        private val jsonDeserializer = Json {
            ignoreUnknownKeys = true
            useAlternativeNames = false
        }

        private val testUpdateJSON = """
        {
          "releasesUrl": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases",
          "releasesLatestUrl": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/latest",
          "mainBranch": {
            "latestRelease": "1.1.3",
            "recommendedRelease": "1.0.2",
            "releases": [
              {
                "ver": "0.6.0",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/v0.6",
                "recommendedNextVer": "0.7.0"
              },
              {
                "ver": "0.6.1",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/v0.6.1"
              },
              {
                "ver": "0.6.3",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/v0.6.3",
                "recommendedNextVer": "0.7.1"
              },
              {
                "ver": "0.7.0",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.0"
              },
              {
                "ver": "0.7.1",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.1"
              },
              {
                "ver": "0.7.99",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.99",
                "recommendedNextVer": "1.1.3",
                "recommendedNextVerManual": true
              },
              {
                "ver": "1.0.1",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.0.1"
              },
              {
                "ver": "1.0.2",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.0.2"
              },
              {
                "ver": "1.0.3",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.0.3"
              },
              {
                "ver": "1.1.0",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.1.0"
              },
              {
                "ver": "1.1.1",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.1.1"
              },
              {
                "ver": "1.1.3",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.1.3"
              }
            ]
          },
          "devBranch": {
            "latestRelease": "1.1.3-beta",
            "recommendedRelease": "0.7.1-beta",
            "releases": [
              {
                "ver": "0.7.0-alpha",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.0-beta"
              },
              {
                "ver": "0.7.0-snapshot",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.0-beta"
              },
              {
                "ver": "0.6.0-beta",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/v0.6",
                "recommendedNextVer": "0.7.1-beta",
                "recommendedNextVerManual": true
              },
              {
                "ver": "0.6.1-beta",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/v0.6"
              },
              {
                "ver": "0.7.1-beta",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.1-beta"
              },
              {
                "ver": "1.0.0-beta",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.1-beta"
              },
              {
                "ver": "1.0.1-beta",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.1-beta"
              },
              {
                "ver": "1.0.2-beta",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.1-beta"
              },
              {
                "ver": "1.1.3-beta",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.1.3-beta"
              },
              {
                "ver": "1.1.3-alpha",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.1.3-alpha"
              },
              {
                "ver": "1.1.3-snapshot",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.1.3-snapshot"
              },
              {
                "ver": "1.1.4-alpha",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/1.1.4-alpha"
              }
            ]
          }
        }
    """.trimIndent()

    }



    @Test
    fun printListCheckResult() {
        //


        println("releasesURL: ${testUpdateCheckResult.releasesURL}")
        println("releasesLatestURL: ${testUpdateCheckResult.releasesLatestURL ?: "Not Found"}")

        println("mainTrack: ")
        testUpdateCheckResult.mainTrack.let { track ->
            println("  latestRelease:      ${track.latestRelease}")
            println("  recommendedRelease: ${track.recommendedRelease ?: "Not Found"}")
            val releases = track.releaseListSortedSet
            println("  --------------------")
            println("  releases: ${releases.count()} total")
            var count = 0
            releases.forEach { rel ->
                count++
                println("    ----------")
                println("    item $count - version:         ${rel.version}")
                println("    item $count - versionSemantic: ${rel.versionSemantic}")
                println("    item $count - pageUrl:         ${rel.pageUrl}")
                println("    item $count - downloadExternalUrl: ${rel.downloadExternalUrl ?: "Not Found"}")
                println("    item $count - downloadEmbeddedUrl: ${rel.downloadEmbeddedUrl ?: "Not Found"}")
                println("    item $count - recommendedNextRelease:          ${rel.recommendedNextRelease ?: "Not Found"}")
                println("    item $count - nextReleaseRequiresManualUpdate: ${rel.recommendedNextReleaseRequiresManualUpdate}")
            }

            println("  --------------------")
        }


        val devTrack = testUpdateCheckResult.devTrack
        if (devTrack == null) {
            println("devTrack: Not Found")
        } else {
            println("devTrack:")
        }

        devTrack?.let { track ->
            println("  latestRelease:      ${track.latestRelease}")
            println("  recommendedRelease: ${track.recommendedRelease ?: "Not Found"}")
            val releases = track.releaseListSortedSet
            println("  --------------------")
            println("  releases: ${releases.count()} total")
            var count = 0
            releases.forEach { rel ->
                count++
                println("    ----------")
                println("    item $count - version:         ${rel.version}")
                println("    item $count - versionSemantic: ${rel.versionSemantic}")
                println("    item $count - pageUrl:         ${rel.pageUrl}")
                println("    item $count - downloadExternalUrl: ${rel.downloadExternalUrl ?: "Not Found"}")
                println("    item $count - downloadEmbeddedUrl: ${rel.downloadEmbeddedUrl ?: "Not Found"}")
                println("    item $count - recommendedNextRelease:          ${rel.recommendedNextRelease ?: "Not Found"}")
                println("    item $count - nextReleaseRequiresManualUpdate: ${rel.recommendedNextReleaseRequiresManualUpdate}")
            }

            println("  --------------------")
        }


    }

    @Test
    fun semVerStringIsPreRelease() {

        val test1 = "1.2.3-beta" // IS Pre-Release
        val result1 = VeadoTouchPlugin.semVerStringIsPreRelease(test1)
        assertTrue(result1)

        val test2 = "1.2.3-beta+abc-123" // IS Pre-Release
        val result2 = VeadoTouchPlugin.semVerStringIsPreRelease(test2)
        assertTrue(result2)


        val test3 = "1.2.3+abc-123" // IS NOT Pre-Release
        val result3 = VeadoTouchPlugin.semVerStringIsPreRelease(test3)
        assertFalse(result3)

        val test4 = "1.2.3" // IS NOT Pre-Release
        val result4 = VeadoTouchPlugin.semVerStringIsPreRelease(test4)
        assertFalse(result4)
    }

    @Test
    fun getMainOrPreReleaseByVersionString(){
        val mainTrackData = testUpdateCheckResult.mainTrack
        val devTrackData = testUpdateCheckResult.devTrack!! //Test Data has DevTrack, assert non-null

        // Exist
        val test1MainExist = "1.1.3"
        val test2DevExist = "1.1.3-beta"

        // Don't Exist
        val test3MainFake = "1.2.3+abc-123"
        val test4DevFake = "1.2.3-beta+abc-123"


        // Exist
        val result1MainExist = VeadoTouchPlugin.getMainOrPreReleaseByVersionString(
            semVerString = test1MainExist,
            mainBranchData = mainTrackData,
            devBranchData = devTrackData
        )
        assertNotNull(result1MainExist)

        val result2DevExist = VeadoTouchPlugin.getMainOrPreReleaseByVersionString(
            semVerString = test2DevExist,
            mainBranchData = mainTrackData,
            devBranchData = devTrackData
        )
        assertNotNull(result2DevExist)


        // Don't Exist
        val result3MainFake = VeadoTouchPlugin.getMainOrPreReleaseByVersionString(
            semVerString = test3MainFake,
            mainBranchData = mainTrackData,
            devBranchData = devTrackData
        )
        assertNull(result3MainFake)

        val result4DevFake = VeadoTouchPlugin.getMainOrPreReleaseByVersionString(
            semVerString = test4DevFake,
            mainBranchData = mainTrackData,
            devBranchData = devTrackData
        )
        assertNull(result4DevFake)


    }

}