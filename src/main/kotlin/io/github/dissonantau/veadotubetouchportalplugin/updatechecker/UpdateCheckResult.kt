package io.github.dissonantau.veadotubetouchportalplugin.updatechecker


import kotlinx.serialization.*


@Serializable
data class UpdateCheckResult(
    /**
     * URL to Release Download Page
     *
     * e.g. "https://github.com/Author/project/releases"
     * */
    @SerialName("releasesUrl")
    val releasesURL: String,
    /** URL to Latest Release Download Page
     *
     * _Optional_
     *
     * e.g. "https://github.com/Author/project/releases/latest"
     *
     * If Null, use [releasesURL] */
    @SerialName("releasesLatestUrl")
    val releasesLatestURL: String? = null,

    /**
     * Main Release Track Data
     *
     * Contains latest & recommended versions, as well as release info for the Main Release
     */
    @SerialName("mainTrack") val mainTrack: ReleaseTrackData,
    /**
     * Dev Release Track Data
     *
     * _Optional_
     *
     * Contains latest & recommended versions, as well as release info for the Development Release
     */
    @SerialName("devTrack") val devTrack: ReleaseTrackData? = null
)

@Serializable
data class ReleaseTrackData(
    /**
     * Version of the Latest Release
     *
     * Should be Semantic Versioning, but could be something else
     */
    @SerialName("latestRelease") val latestRelease: String,
    /**
     * Version of the Recommended Release
     *
     * _Optional_
     *
     * Should be Semantic Versioning, but could be something else
     *
     * If null, use [latestRelease]
     */
    @SerialName("recommendedRelease") val recommendedRelease: String? = null,
    /**
     * Release Data for different releases
     *
     * Should contain referenced versions, but not guaranteed
     *
     * If null, compare current version to [latestRelease] and recommend updating to that.
     *
     * If a version is missing, use the next minor version in the same major version and check for a
     * [ReleaseData.recommendedNextRelease], then try last minor version or next major version.
     *  the Latest Release should be [UpdateCheckResult.releasesLatestURL]
     */
    @SerialName("releases") val releaseList: List<ReleaseData>? = null
)

@Serializable
data class ReleaseData(
    /**
     * Version of this Release
     *
     * Should be Semantic Versioning, but could be something else
     */
    @SerialName("version") val version: String,
    /**
     * URL to download Page for this Version
     */
    @SerialName("url") val url: String,
    /**
     * Recommended Next Release
     *
     * _Optional_
     *
     * Only set if plugin should be updated to an intermediate version before updating to the latest.
     *
     * If null, use [ReleaseTrackData.recommendedRelease]
     */
    @SerialName("recommendedNextRelease") val recommendedNextRelease: String? = null
)
