package io.github.dissonantau.veadotubetouchportalplugin.updatechecker


import io.github.dissonantau.veadotubetouchportalplugin.BuildConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.swiftzer.semver.SemVer


class PluginUpdateChecker(private val listener: UpdateCheckResultListener, private val updateCheckReleasesUri: String) {


    companion object {
        /* Start of functions for Update Checks */
        @JvmStatic
        private val LOGGER =
            KotlinLogging.logger { PluginUpdateChecker::class.java.name }

        /**
         * Calculate and return Update Values for Update Notification
         */
        @JvmOverloads
        fun calculateUpdates(
            resultData: UpdateCheckResult,
            // Current Release String (SemVer Format)
            currentReleaseVersionString: String = BuildConfig.VERSION_NAME_FULL,
            // Main Branch Recommended SemVer
            recommendedMainReleaseString: String =
                resultData.mainTrack.recommendedRelease ?: resultData.mainTrack.latestRelease,
            // If Build is Main Branch Release
            buildIsRelease: Boolean = BuildConfig.BUILD_IS_RELEASE,
            // Dev Branch Recommended SemVer
            recommendedDevRelease: String? = null
        ): UpdateReleaseData {
            // Current Release SemVer
            val currentReleaseSemVer = SemVer.parse(currentReleaseVersionString)
            // Get Main Release SemVer
            val recommendedMainReleaseSemVer = SemVer.parse(recommendedMainReleaseString)

            LOGGER.trace { "onUpdateCheckResult - Current: $currentReleaseVersionString - Recommended: $recommendedMainReleaseString" }

            // Object to hold Calculated Update Release info
            val updateData = UpdateReleaseData()

            // Mainline Release Version Update Check
            if (recommendedMainReleaseString != currentReleaseVersionString) {
                // Version Strings don't match

                // Check Version number is higher
                if (currentReleaseSemVer < recommendedMainReleaseSemVer) {
                    // Recommended Main Release SemVer is newer then current
                    LOGGER.debug { "onUpdateCheckResult - Current Version ($currentReleaseVersionString) Is Older than Recommended ($recommendedMainReleaseString)" }

                    calculateNextUpdateRelease(
                        updateData,
                        currentReleaseSemVer,
                        recommendedMainReleaseSemVer,
                        resultData,
                    )
                }
            }

            // Dev/Pre-release Version Update Check
            if (!buildIsRelease && resultData.devTrack != null) {
                // Running Dev Release
                // If not provided by parameter, try getting Dev Branch Recommended Release, then Dev Branch Latest
                val recommendedDevReleaseString: String =
                    recommendedDevRelease ?: resultData.devTrack.recommendedRelease ?: resultData.devTrack.latestRelease

                if (recommendedDevReleaseString != currentReleaseVersionString) {
                    // Strings don't match
                    val recommendedDevReleaseSemVer = SemVer.parse(recommendedDevReleaseString)

                    // Check New Version is higher
                    if (currentReleaseSemVer < recommendedDevReleaseSemVer) {
                        // Recommended Main Release SemVer is newer then current
                        LOGGER.debug { "onUpdateCheckResult - Current Dev Version ($currentReleaseVersionString) Is Older than Recommended Dev ($recommendedDevReleaseSemVer)" }

                        calculateNextUpdateDev(
                            updateData,
                            currentReleaseSemVer,
                            recommendedMainReleaseSemVer,
                            recommendedDevReleaseSemVer,
                            resultData,
                        )
                    }
                }
            }

            return updateData
        }


        fun calculateNextUpdateRelease(
            updateData: UpdateReleaseData,
            currentRelease: SemVer,
            recommendedBranchRelease: SemVer,
            updateCheckResult: UpdateCheckResult
        ) {

            val currentReleaseMajorVer = currentRelease.major
            val currentRecommendedIsSameMajorVer =
                currentReleaseMajorVer == recommendedBranchRelease.major

            // Scan Version list of Major Version for any Recommended Next Versions
            val versionListMapString = updateCheckResult.mainTrack.releaseMapByVersionString
            val versionListGroupMap = updateCheckResult.mainTrack.releaseListGroupMap

            // Try and get current version from the Release List
            versionListMapString[currentRelease.toString()]?.let { currentReleaseData ->
                // Release Matching Current found > Find Recommended release

                currentReleaseData.recommendedNextRelease?.let { recommendedNextString ->
                    // Recommended version found, get release info
                    versionListMapString[recommendedNextString]?.let {
                        updateData.mainBranchReleaseData = it
                        updateData.mainBranchUpdateAvailable = true
                        updateData.mainBranchManualUpdateRequired =
                            currentReleaseData.recommendedNextReleaseRequiresManualUpdate
                        return
                    }
                }
            }
            // Continues if Current Release doesn't have Recommended Next Release or version info not found


            // Go through update list and look for next recommended updates
            // the same major version track
            versionListGroupMap[currentReleaseMajorVer]?.listIterator()?.let { listIterator ->
                for (release in listIterator) {
                    // Skip if less than current
                    if (release.versionSemantic < currentRelease) continue
                    // If Next Ver is Same Major Version, break if we pass it
                    if (currentRecommendedIsSameMajorVer && release.versionSemantic > recommendedBranchRelease) break

                    // If we find an exact match for recommended version while scanning, return it
                    if (currentRecommendedIsSameMajorVer && release.versionSemantic == recommendedBranchRelease) {
                        updateData.mainBranchReleaseData = release
                        updateData.mainBranchUpdateAvailable = true
                        return
                    }
                    // Try and find recommendedNextRelease - continues if string is null, or string doesn't match a next version
                    release.recommendedNextRelease?.let { recommendedNextString ->
                        // Recommended version found, get release info
                        versionListMapString[recommendedNextString]?.let {
                            updateData.mainBranchReleaseData = it
                            updateData.mainBranchUpdateAvailable = true
                            updateData.mainBranchManualUpdateRequired =
                                release.recommendedNextReleaseRequiresManualUpdate
                            return
                        }
                    }
                }
            }

            // If updateRelease not set by now, fall back to recommendedMainRelease
            versionListMapString[recommendedBranchRelease.toString()]?.let {
                updateData.mainBranchReleaseData = it
                updateData.mainBranchUpdateAvailable = true
            }
        }


        fun calculateNextUpdateDev(
            updateData: UpdateReleaseData,
            currentRelease: SemVer,
            recommendedMainBranchRelease: SemVer?,
            recommendedDevBranchRelease: SemVer?,
            updateCheckResult: UpdateCheckResult
        ) {
            // If recommended releases are the same, return null (Main Ver check returns same)
            if (recommendedMainBranchRelease == recommendedDevBranchRelease) {
                //updateData.devBranchReleaseData = null
                return
            }

            // If null, we can't find any recommended updates (Also smart casts to non-nullable
            if (updateCheckResult.devTrack == null) {
                //updateData.devBranchReleaseData = null
                return
            }

            val currentReleaseMajorVer = currentRelease.major

            val currentRecommendedDevSameMajorVer =
                currentReleaseMajorVer == recommendedDevBranchRelease?.major

            // Try and get current version from the Release List
            updateCheckResult.devTrack.releaseMapByVersionString[currentRelease.toString()]
                ?.let { currentReleaseData ->
                    // Release Matching Current found > Find Recommended release

                    currentReleaseData.recommendedNextRelease?.let { recommendedNextString ->
                        // Recommended version found, get release info
                        updateData.devBranchReleaseData =
                            getMainOrPreReleaseByVersionString(
                                recommendedNextString,
                                updateCheckResult.mainTrack,
                                updateCheckResult.devTrack
                            )
                        updateData.devBranchUpdateAvailable = true
                        updateData.devBranchManualUpdateRequired =
                            currentReleaseData.recommendedNextReleaseRequiresManualUpdate
                        return
                    }
                }
            // Continues if Current Release doesn't have Recommended Next Release or version info not found


            val versionListGroupMap = updateCheckResult.devTrack.releaseListGroupMap
            var recommendedVersionString: String? = null
            var recommendedVersionStringRequiresManualUpdate = false

            // Go through update list and look for next recommended updates
            // the same major version track
            versionListGroupMap[currentReleaseMajorVer]?.listIterator()?.let { listIterator ->
                for (release in listIterator) {
                    // Skip if less than current
                    if (release.versionSemantic < currentRelease) continue

                    // If Next Ver is Same Major Version, break if we pass it
                    if (currentRecommendedDevSameMajorVer && recommendedDevBranchRelease != null &&
                        release.versionSemantic > recommendedDevBranchRelease
                    ) break

                    // If we find an exact match for recommended version while scanning, return it
                    if (currentRecommendedDevSameMajorVer && release.versionSemantic == recommendedDevBranchRelease) {
                        updateData.devBranchReleaseData = release
                        return
                    }

                    // Try and find recommendedNextRelease
                    if (release.recommendedNextRelease != null) {
                        // Recommended version found, get release info
                        recommendedVersionString = release.recommendedNextRelease
                        recommendedVersionStringRequiresManualUpdate =
                            release.recommendedNextReleaseRequiresManualUpdate
                        break
                    }
                }
            }


            // Ver string - check if pre-release, then try and find the version and return
            recommendedVersionString?.let { recommendedVerString ->
                updateData.devBranchReleaseData =
                    getMainOrPreReleaseByVersionString(
                        recommendedVerString,
                        updateCheckResult.mainTrack,
                        updateCheckResult.devTrack
                    )
                updateData.devBranchUpdateAvailable = true
                updateData.devBranchManualUpdateRequired = recommendedVersionStringRequiresManualUpdate
                return
            }

        }

        /**
         * Checks semVerString:
         * - If pre-release semVer, looks in devBranchData.releaseMapByVersionString for Release matching String.
         * - If not pre-release semVer, looks in mainBranchData.releaseMapByVersionString for Release matching String.
         *
         * If no match found, returns null
         */
        fun getMainOrPreReleaseByVersionString(
            semVerString: String,
            mainBranchData: ReleaseBranchData,
            devBranchData: ReleaseBranchData
        ): ReleaseData? {
            return if (semVerStringIsPreRelease(semVerString))
                devBranchData.releaseMapByVersionString[semVerString]
            else // Isn't Pre-release
                mainBranchData.releaseMapByVersionString[semVerString]
        }

        /**
         * Quick check for seeing if SemVer String is pre-release (has a dash before any +)
         *
         * e.g.
         * - 1.2.3-beta -> IS Pre-Release
         * - 1.2.3+abc-123 -> IS ***NOT*** Pre-Release
         * - 1.2.3-beta+abc-123 -> IS Pre-Release
         *
         */
        fun semVerStringIsPreRelease(semVerString: String): Boolean {
            val dashPos = semVerString.indexOf('-')
            if (dashPos > 0) {
                // If String contains dash, could be a pre-release
                val plusPos = semVerString.indexOf('+')

                // Plus also exists, and isn't after first dash - definitely pre-release
                return (plusPos < 0 || dashPos < plusPos)
            }
            return false
        }

        /* End of functions for Update Checks */

    }

    /**
     * JSON De/serializer
     */
    private val jsonDeserializer = Json {
        ignoreUnknownKeys = true
        useAlternativeNames = false
    }

    /**
     * Update Checker Job - Coroutine Job launched to check for updates
     */
    private var checkerJob: Job

    /**
     * Context for this Update Checker
     */
    private val checkerCoroutineContext: CoroutineContext =
        CoroutineName(name = "pluginUpdateCheck-cor") + Dispatchers.IO

    /**
     * Scope for this Update Checker, used to launch Jobs
     */
    private val checkerCoroutineScope: CoroutineScope = CoroutineScope(checkerCoroutineContext)


    init {
        checkerJob = checkerCoroutineScope.launch {
            yield()
            runUpdateCheck()
        }
    }


    /**
     * Check for newer Plugin Versions
     */
    private suspend fun runUpdateCheck() {
        val httpClient = HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = if (LOGGER.isDebugEnabled()) LogLevel.INFO else LogLevel.NONE
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                retryOnException(maxRetries = 3)
                exponentialDelay()
            }
        }


        try {
            // Get Releases JSON
            val response: HttpResponse = httpClient.get(updateCheckReleasesUri)

            if (response.status != HttpStatusCode.OK) {
                // TODO Test if HTTP GET Error is thrown or if this is enough
                LOGGER.warn { "Error Checking for updates: ${response.status.description}" }
            } else {
                val responseJSONText = response.bodyAsText()

                // Decode and Convert JSON to Object
                LOGGER.trace { "runUpdateCheck: Attempting to decode JSON String to object:\n$responseJSONText" }

                val convertedMessage: UpdateCheckResult =
                    try {
                        jsonDeserializer.decodeFromString(responseJSONText)
                    } catch (thrown: Throwable) {
                        val warningString = StringBuilder()

                        if (thrown !is Exception) {
                            // Major Error - Throwable instead of Exception, could be something like NoClassDefFoundError or ExceptionInInitializerError
                            warningString.append("Major Error deserializing JSON String to UpdateCheckResult")
                        } else {
                            // Regular Exception
                            warningString.append("Failed to deserialize JSON String to UpdateCheckResult")
                        }

                        LOGGER.debug { "runUpdateCheck: Unable to decode JSON String to UpdateCheckResult: ${thrown.message}\n$responseJSONText" }

                        try {
                            //Try to convert to generic JSON Element - this isn't passed, but will let us know if it's valid JSON
                            val jsonMessage = Json.parseToJsonElement(responseJSONText)
                            warningString.append("; Successfully decoded to Generic JSON Element")
                            LOGGER.warn { "Warning - Failed to Deserialize JSON to Object: $jsonMessage" }

                        } catch (exInner: Exception) {
                            warningString.append("; Failed decode to Generic JSON Element")
                            LOGGER.warn { "Warning - Failed to Deserialize JSON. Error: '${exInner.message}' > JSON: '$responseJSONText'" }
                            //Add as Suppressed Exception
                            thrown.addSuppressed(exInner)
                        }

                        val exception = Exception(warningString.toString(), thrown)
                        throw exception
                    }


                // Calculate Update Version(s)
                val updateData = calculateUpdates(convertedMessage)

                // Send to Listener
                listener.onUpdateCheckResult(updateData)
            }

        } catch (ex: Exception) {
            // Send error to Listener
            listener.onUpdateCheckError(ex)
        } finally {
            httpClient.close()
        }

    }


    fun close() {
        checkerJob.cancel()
    }
}