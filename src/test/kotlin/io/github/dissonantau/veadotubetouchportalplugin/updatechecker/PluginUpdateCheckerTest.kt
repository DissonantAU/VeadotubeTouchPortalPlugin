package io.github.dissonantau.veadotubetouchportalplugin.updatechecker


import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled


class PluginUpdateCheckerTest {

    companion object {

        /** Class Logger */
        @JvmStatic
        private val LOGGER =
            KotlinLogging.logger { PluginUpdateCheckerTest::class.java.name }

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
            "recommendedRelease": "0.7.2-beta",
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
                "ver": "0.6.0-alpha",
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/v0.6"
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
                "url": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.1-beta",
                "urlDlExternal": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/download/0.7.1-beta/VeadoTouchPlugin_0.7.1-beta.20241210+debug.tpp",
                "urlDlBundled": "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/download/0.7.1-beta/VeadoTouchPlugin_0.7.1-beta.20241210+internalJava-debug.tpp"
              },
              {
                "ver": "0.7.2-beta",
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


    @Disabled
    @Test
    fun onUpdateCheckResult() {
        //Currently just copied from onUpdateCheckResult


    }


    @Test
    fun printReleaseCheckResult() {
        println("releasesURL: ${testUpdateCheckResult.releasesURL}")
        println("releasesLatestURL: ${testUpdateCheckResult.releasesLatestURL ?: "Not Found"}")

        println("mainTrack: ")
        testUpdateCheckResult.mainBranch.let { track ->
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
                println("    item $count - pageUrl:         ${rel.downloadUrlPage}")
                println("    item $count - downloadExternalUrl: ${rel.downloadUrlExternal ?: "Not Found"}")
                println("    item $count - downloadBundledUrl: ${rel.downloadUrlBundled ?: "Not Found"}")
                println("    item $count - recommendedNextRelease:          ${rel.recommendedNextRelease ?: "Not Found"}")
                println("    item $count - nextReleaseRequiresManualUpdate: ${rel.recommendedNextReleaseRequiresManualUpdate}")
            }

            println("  --------------------")
        }


        val devTrack = testUpdateCheckResult.devBranch
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
                println("    item $count - pageUrl:         ${rel.downloadUrlPage}")
                println("    item $count - downloadExternalUrl: ${rel.downloadUrlExternal ?: "Not Found"}")
                println("    item $count - downloadBundledUrl: ${rel.downloadUrlBundled ?: "Not Found"}")
                println("    item $count - recommendedNextRelease:          ${rel.recommendedNextRelease ?: "Not Found"}")
                println("    item $count - nextReleaseRequiresManualUpdate: ${rel.recommendedNextReleaseRequiresManualUpdate}")
            }

            println("  --------------------")
        }


    }


    //@Disabled
    @Test
    fun calculateUpdates() {
        println("##############################")
        println("calculateUpdates Start")

        val testData = testUpdateCheckResult
        val mainTrackData = testUpdateCheckResult.mainBranch
        val devTrackData = testUpdateCheckResult.devBranch!! //Test Data has DevTrack, assert non-null

        val latestRelease = "1.1.3"
        val latestReleaseSemVer = SemVer.parse(latestRelease)

        val recommendedRelease = "1.0.2"
        val recommendedReleaseSemVer = SemVer.parse(recommendedRelease)

        val latestDevRelease = "1.1.3-beta"
        val latestDevReleaseSemVer = SemVer.parse(latestDevRelease)

        val recommendedDevRelease = "0.7.2-beta"
        val recommendedDevReleaseSemVer = SemVer.parse(recommendedDevRelease)

        listOf(
            CalcUpdateTest("0.6.0", "0.7.0"),
            CalcUpdateTest("0.6.1", "0.7.1"), // 0.6.1 has no recommended, but 0.6.3 does
            CalcUpdateTest("0.6.3", "0.7.1"), // Recommended is 0.7.1

            // doesn't exist in list, should use 0.7.99 (next in same major ver)
            CalcUpdateTest("0.6.4", "1.1.3", true),
            CalcUpdateTest("0.6.5-beta", "1.1.3", true, "0.7.2-beta"),

            CalcUpdateTest("0.7.0", "1.1.3", true), // 0.7.0 has no recommended, but 0.7.99 does
            CalcUpdateTest("0.7.1", "1.1.3", true), // 0.7.1 has no recommended, but 0.7.99 does
            CalcUpdateTest("0.7.2", "1.1.3", true), // doesn't exist in list, should use 0.7.99
            CalcUpdateTest("0.7.99", "1.1.3", true),

            // No recommended, should use branch recommendedRelease
            CalcUpdateTest("1.0.1-beta", recommendedRelease),
            CalcUpdateTest("1.0.1", recommendedRelease), // No recommended, older than branch recommended
            CalcUpdateTest("1.0.2", null), // No recommended, is branch recommended

            // TODO Review - Maybe should recommend latestRelease in SubVer if current is newer than recommendedRelease?
            CalcUpdateTest("1.0.3", null), // No recommended, is newer than branch recommended

            CalcUpdateTest("1.1.0", null), // No recommended, is newer than branch recommended
            CalcUpdateTest("1.1.1", null), // No recommended, is newer than branch recommended
            CalcUpdateTest("1.1.3", null), // No recommended, is newer than branch recommended

            // Beta
            // Main Ver "0.6.0" recommendedNextVer is "0.7.0" - Dev Ver "0.6.0-beta" recommendedNextVer is "0.7.1-beta"
            CalcUpdateTest("0.6.0-beta", "0.7.0", false, "0.7.1-beta", true),
            // Main Ver "0.6.3" recommendedNextVer is "0.7.1" - Dev recommendedRelease is "0.7.2-beta"
            CalcUpdateTest("0.6.1-beta", "0.7.1", false, "0.7.2-beta"),

            // Main Ver "0.7.99" recommendedNextVer is "1.1.3" & NextVerManual = true - Dev recommendedRelease is "0.7.2-beta"
            CalcUpdateTest("0.7.0-alpha", "1.1.3", true, "0.7.2-beta"),
            CalcUpdateTest("0.7.0-snapshot", "1.1.3", true, "0.7.2-beta"),
            CalcUpdateTest("0.7.1-beta", "1.1.3", true, "0.7.2-beta"),

            // TODO Review - Maybe should recommend latestRelease in SubVer if current is newer than recommendedRelease?
            // Main recommendedRelease is "1.0.2" - Dev recommendedRelease is "0.7.1-beta"
            CalcUpdateTest("1.0.0-beta", "1.0.2", false, null),
            CalcUpdateTest("1.0.5-beta", null, false, null),

            CalcUpdateTest("1.1.3-beta", null, false, null),

            ).forEach { testInfo ->
            println("--------------------")
            println("Test - Starting Ver: ${testInfo.startingVer}; Expected Next Main Ver: ${testInfo.expectedNextMainVer ?: "null"}; Expected Next Dev Ver: ${testInfo.expectedNextDevVer ?: "null"}")

            val startVerIsRelease: Boolean = testInfo.startingSemVer.preRelease == null

            val updateData = PluginUpdateChecker.calculateUpdates(
                resultData = testUpdateCheckResult,
                currentReleaseVersionString = testInfo.startingSemVer.toString(),
                buildIsRelease = startVerIsRelease,
            )


            if (updateData.updateAvailable) {
                println("       Update Available: ${updateData.updateAvailable}")

            }


            val verMainData = updateData.mainBranchReleaseData
            val verMainManInstall = updateData.mainBranchManualUpdateRequired

            val equalVerMain = testInfo.expectedNextMainSemVer == verMainData?.versionSemantic
            println("       Main - Next Version: ${verMainData?.version ?: "null"}; Expected: ${testInfo.expectedNextMainVer ?: "null"}; Equal: $equalVerMain")
            assertTrue(equalVerMain)

            val equalMain = testInfo.expectedNextMainVerManualInstall == verMainManInstall
            println("       Main - Manual Update: ${verMainManInstall}; Expected: ${testInfo.expectedNextMainVerManualInstall}; Equal: $equalMain")
            assertTrue(equalMain)


            val verDevData = updateData.devBranchReleaseData
            val verDevManInstall = updateData.devBranchManualUpdateRequired

            val equalVerDev = testInfo.expectedNextDevSemVer == verDevData?.versionSemantic
            println("       Dev  - Next Version: ${verDevData?.version ?: "null"}; Expected: ${testInfo.expectedNextDevVer ?: "null"}; Equal: $equalVerDev")
            assertTrue(equalVerDev)

            val equalDev = testInfo.expectedNextDevVerManualInstall == verDevManInstall
            println("       Dev  - Manual Update: ${verDevManInstall}; Expected: ${testInfo.expectedNextDevVerManualInstall}; Equal: $equalDev")
            assertTrue(equalDev)


        }

        println("--------------------")
        println("calculateUpdates End")
        println("##############################")
    }


    @Test
    fun calculateNextUpdateRelease() {
        println("##############################")
        println("calculateNextUpdateRelease Start")

        val testData = testUpdateCheckResult
        val mainTrackData = testUpdateCheckResult.mainBranch
        val devTrackData = testUpdateCheckResult.devBranch!! //Test Data has DevTrack, assert non-null

        val latestRelease = "1.1.3"
        val latestReleaseSemVer = SemVer.parse(latestRelease)

        val recommendedRelease = "1.0.2"
        val recommendedReleaseSemVer = SemVer.parse(recommendedRelease)

        listOf(
            CalcNextUpdateTest("0.6.0", "0.7.0"),
            CalcNextUpdateTest("0.6.1", "0.7.1"), // 0.6.1 has no recommended, but 0.6.3 does
            CalcNextUpdateTest("0.6.3", "0.7.1"), // Recommended is 0.7.1

            // doesn't exist in list, should use 0.7.99 (next in same major ver)
            CalcNextUpdateTest("0.6.4", "1.1.3", true),
            CalcNextUpdateTest("0.6.5-beta", "1.1.3", true),

            CalcNextUpdateTest("0.7.0", "1.1.3", true), // 0.7.0 has no recommended, but 0.7.99 does
            CalcNextUpdateTest("0.7.1", "1.1.3", true), // 0.7.1 has no recommended, but 0.7.99 does
            CalcNextUpdateTest("0.7.2", "1.1.3", true), // doesn't exist in list, should use 0.7.99
            CalcNextUpdateTest("0.7.99", "1.1.3", true),

            // No recommended, should use branch recommendedRelease
            CalcNextUpdateTest("1.0.1-beta", recommendedRelease),
            CalcNextUpdateTest("1.0.1", recommendedRelease), // No recommended
            CalcNextUpdateTest("1.0.2", recommendedRelease), // No recommended, is branch recommended
            CalcNextUpdateTest("1.0.3", recommendedRelease), // No recommended, is newer than branch recommended

            CalcNextUpdateTest("1.1.0", recommendedRelease), // No recommended, should use branch recommendedRelease
            CalcNextUpdateTest("1.1.1", recommendedRelease), // No recommended, is branch recommended
            CalcNextUpdateTest("1.1.3", recommendedRelease), // No recommended, is newer than branch recommended

        ).forEach { testInfo ->
            println("--------------------")
            println("Test - Starting Ver: ${testInfo.startingVer}; Expected Next Ver: ${testInfo.expectedNextVer ?: "null"}")

            val updateData = UpdateReleaseData()
            PluginUpdateChecker.calculateNextUpdateRelease(
                updateData,
                currentRelease = testInfo.startingSemVer,
                recommendedBranchRelease = recommendedReleaseSemVer,
                updateCheckResult = testUpdateCheckResult
            )

            val verData = updateData.mainBranchReleaseData
            val verManInstall = updateData.mainBranchManualUpdateRequired

            val equalVer = testInfo.expectedNextSemVer == verData?.versionSemantic
            println("       Next Version: ${verData?.version ?: "null"}; Expected: ${testInfo.expectedNextVer ?: "null"}; Equal: $equalVer")
            assertTrue(equalVer)

            val equal = testInfo.expectedNextVerManualInstall == verManInstall
            println("       Manual Update: ${verManInstall}; Expected: ${testInfo.expectedNextVerManualInstall}; Equal: $equal")
            assertTrue(equal)

        }

        println("--------------------")
        println("calculateNextUpdateRelease End")
        println("##############################")
    }


    @Test
    fun calculateNextUpdateDev() {

        println("##############################")
        println("calculateNextUpdateDev Start")

        val mainTrackData = testUpdateCheckResult.mainBranch
        val devTrackData = testUpdateCheckResult.devBranch!! //Test Data has DevTrack, assert non-null

        val latestRelease = "1.1.3"
        val latestReleaseSemVer = SemVer.parse(latestRelease)

        val recommendedRelease = "1.0.2"
        val recommendedReleaseSemVer = SemVer.parse(recommendedRelease)

        val latestDevRelease = "1.1.3-beta"
        val latestDevReleaseSemVer = SemVer.parse(latestDevRelease)

        val recommendedDevRelease = "0.7.2-beta"
        val recommendedDevReleaseSemVer = SemVer.parse(recommendedDevRelease)

        listOf(
            CalcNextUpdateTest("0.6.0-beta", "0.7.1-beta", true),
            CalcNextUpdateTest("0.6.1-beta", "0.7.2-beta"), // has no recommended

            CalcNextUpdateTest("0.7.0-alpha", "0.7.2-beta"),

            CalcNextUpdateTest("0.7.0-snapshot", "0.7.2-beta"),

            CalcNextUpdateTest("0.7.1-beta", "0.7.2-beta"),

            CalcNextUpdateTest("1.0.0-beta", null),
            CalcNextUpdateTest("1.0.5-beta", null),

            CalcNextUpdateTest("1.1.3-beta", null),

            ).forEach { testInfo ->
            println("--------------------")
            println("Test - Starting Ver: ${testInfo.startingVer}; Expected Starting Ver: ${testInfo.expectedNextVer ?: "null"}")

            val updateData = UpdateReleaseData()
            PluginUpdateChecker.calculateNextUpdateDev(
                updateData,
                currentRelease = testInfo.startingSemVer,
                recommendedMainBranchRelease = recommendedReleaseSemVer,
                recommendedDevBranchRelease = recommendedDevReleaseSemVer,
                updateCheckResult = testUpdateCheckResult
            )

            val verData = updateData.devBranchReleaseData
            val verManInstall = updateData.devBranchManualUpdateRequired

            val equalVer = testInfo.expectedNextSemVer == verData?.versionSemantic
            println("       Next Version: ${verData?.version ?: "null"}; Expected: ${testInfo.expectedNextVer ?: "null"}; Equal: $equalVer")
            assertTrue(equalVer)

            val equal = testInfo.expectedNextVerManualInstall == verManInstall
            println("       Manual Update: ${verManInstall}; Expected: ${testInfo.expectedNextVerManualInstall}; Equal: $equal")
            assertTrue(equal)

        }

        println("--------------------")
        println("calculateNextUpdateDev End")
        println("##############################")
    }

    @Test
    fun checkURLs() {
        val expectedReleasesUrl = "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases"
        val expectedReleasesLatestUrl = "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/latest"

        // Strings
        val releasesURL = testUpdateCheckResult.releasesURL
        val releasesLatestURL = testUpdateCheckResult.releasesLatestURL

        assertEquals(releasesURL, expectedReleasesUrl)
        assertEquals(releasesLatestURL, expectedReleasesLatestUrl)

        // URLs
        val urlReleases = testUpdateCheckResult.urlReleases
        val urlReleasesLatest = testUpdateCheckResult.urlReleasesLatest

        assertEquals(urlReleases.toString(), expectedReleasesUrl)
        assertNotNull(urlReleasesLatest)
        assertEquals(urlReleasesLatest?.toString(), expectedReleasesLatestUrl)

        // Release Data
        val verWithURLs = "0.7.1-beta"

        val expectedUrl = "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/tag/0.7.1-beta"
        val expectedUrlDlExternal =
            "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/download/0.7.1-beta/VeadoTouchPlugin_0.7.1-beta.20241210+debug.tpp"
        val expectedUrlDlBundled =
            "https://github.com/DissonantAU/VeadotubeTouchPortalPlugin/releases/download/0.7.1-beta/VeadoTouchPlugin_0.7.1-beta.20241210+internalJava-debug.tpp"

        val devTrackData = testUpdateCheckResult.devBranch!! //Test Data has DevTrack, assert non-null

        val verToCheck = devTrackData.releaseMapByVersionString[verWithURLs]

        assertNotNull(verToCheck)

        if (verToCheck != null) {
            // Strings
            val downloadUrlPage = verToCheck.downloadUrlPage
            val downloadUrlExternal = verToCheck.downloadUrlExternal
            val downloadUrlBundled = verToCheck.downloadUrlBundled

            assertEquals(downloadUrlPage, expectedUrl)
            assertEquals(downloadUrlExternal, expectedUrlDlExternal)
            assertEquals(downloadUrlBundled, expectedUrlDlBundled)

            // URLs
            val urlDownloadPage = verToCheck.urlDownloadPage
            val downloadExternalUrl = verToCheck.urlDownloadExternal
            val downloadBundledUrl = verToCheck.urlDownloadBundled

            assertEquals(urlDownloadPage.toString(), expectedUrl)
            assertNotNull(downloadExternalUrl)
            assertEquals(downloadExternalUrl?.toString(), expectedUrlDlExternal)
            assertNotNull(downloadBundledUrl)
            assertEquals(downloadBundledUrl?.toString(), expectedUrlDlBundled)
        }

    }

    @Test
    fun semVerStringIsPreRelease() {

        val test1 = "1.2.3-beta" // IS Pre-Release
        val result1 = PluginUpdateChecker.semVerStringIsPreRelease(test1)
        assertTrue(result1)

        val test2 = "1.2.3-beta+abc-123" // IS Pre-Release
        val result2 = PluginUpdateChecker.semVerStringIsPreRelease(test2)
        assertTrue(result2)


        val test3 = "1.2.3+abc-123" // IS NOT Pre-Release
        val result3 = PluginUpdateChecker.semVerStringIsPreRelease(test3)
        assertFalse(result3)

        val test4 = "1.2.3" // IS NOT Pre-Release
        val result4 = PluginUpdateChecker.semVerStringIsPreRelease(test4)
        assertFalse(result4)
    }

    @Test
    fun getMainOrPreReleaseByVersionString() {
        val mainTrackData = testUpdateCheckResult.mainBranch
        val devTrackData = testUpdateCheckResult.devBranch!! //Test Data has DevTrack, assert non-null

        // Exist
        val test1MainExist = "1.1.3"
        val test2DevExist = "1.1.3-beta"

        // Don't Exist
        val test3MainFake = "1.2.3+abc-123"
        val test4DevFake = "1.2.3-beta+abc-123"


        // Exist
        val result1MainExist = PluginUpdateChecker.getMainOrPreReleaseByVersionString(
            semVerString = test1MainExist,
            mainBranchData = mainTrackData,
            devBranchData = devTrackData
        )
        assertNotNull(result1MainExist)

        val result2DevExist = PluginUpdateChecker.getMainOrPreReleaseByVersionString(
            semVerString = test2DevExist,
            mainBranchData = mainTrackData,
            devBranchData = devTrackData
        )
        assertNotNull(result2DevExist)


        // Don't Exist
        val result3MainFake = PluginUpdateChecker.getMainOrPreReleaseByVersionString(
            semVerString = test3MainFake,
            mainBranchData = mainTrackData,
            devBranchData = devTrackData
        )
        assertNull(result3MainFake)

        val result4DevFake = PluginUpdateChecker.getMainOrPreReleaseByVersionString(
            semVerString = test4DevFake,
            mainBranchData = mainTrackData,
            devBranchData = devTrackData
        )
        assertNull(result4DevFake)


    }

}

data class CalcNextUpdateTest(
    val startingVer: String,
    val expectedNextVer: String?,
    val expectedNextVerManualInstall: Boolean = false
) {
    val startingSemVer = SemVer.parse(startingVer)
    val expectedNextSemVer = expectedNextVer?.let { SemVer.parse(it) }
}

data class CalcUpdateTest(
    val startingVer: String,
    val expectedNextMainVer: String?,
    val expectedNextMainVerManualInstall: Boolean = false,
    val expectedNextDevVer: String? = null,
    val expectedNextDevVerManualInstall: Boolean = false
) {
    val startingSemVer = SemVer.parse(startingVer)
    val startingVerBuildIsRelease = (startingSemVer.preRelease == null)
    val expectedNextMainSemVer = expectedNextMainVer?.let { SemVer.parse(it) }
    val expectedNextDevSemVer = expectedNextDevVer?.let { SemVer.parse(it) }
}
