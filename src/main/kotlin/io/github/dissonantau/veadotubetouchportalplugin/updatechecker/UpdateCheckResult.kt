@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.dissonantau.veadotubetouchportalplugin.updatechecker


import kotlinx.serialization.*
import net.swiftzer.semver.SemVer
import java.util.*


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
     * If Null, use [releasesURL]
     */
    @SerialName("releasesLatestUrl")
    val releasesLatestURL: String? = null,

    /**
     * Main Release Branch Data
     *
     * Contains latest & recommended versions, as well as release info for the Main Release
     */
    @SerialName("mainBranch") val mainTrack: ReleaseBranchData,
    /**
     * Dev Release Branch Data
     *
     * _Optional_
     *
     * Contains latest & recommended versions, as well as release info for the Development Release
     */
    @SerialName("devBranch") val devTrack: ReleaseBranchData? = null
)

@Serializable
data class ReleaseBranchData(
    /**
     * Version of the Latest Release
     *
     * Should follow [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
     */
    @SerialName("latestRelease") val latestRelease: String,
    /**
     * Version of the Recommended Release
     *
     * _Optional_
     *
     * Should follow [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
     *
     * If ***null***, use [latestRelease]
     */
    @SerialName("recommendedRelease") val recommendedRelease: String? = null,
    /**
     * Release Data for different releases
     *
     * Should follow [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
     *
     * If empty, compare current version to [latestRelease] and recommend updating to that.
     *
     * If a version is missing, use the next minor version in the same major version and check for a
     * [ReleaseData.recommendedNextRelease], then try last minor version or next major version.
     *  the Latest Release should be [UpdateCheckResult.releasesLatestURL]
     */
    @SerialName("releases") val releaseList: List<ReleaseData> = emptyList(),

    ) {

    /** Comparator for Sorting by [ReleaseData.versionSemantic] */
    val releaseDataSemVerComparator = compareBy<ReleaseData> { it.versionSemantic }

    /**
     * [releaseList] sorted by [ReleaseData.versionSemantic]
     */
    val releaseListSortedSet: SortedSet<ReleaseData> by lazy {
        releaseList.toSortedSet(releaseDataSemVerComparator)
    }

    /**
     * [releaseList] sorted by [ReleaseData.versionSemantic], grouped by [SemVer.major]
     *
     * Generated from [releaseListSortedSet] - Keys and Lists should be sorted ascending, following Semantic Versioning Order
     *
     * * Key: Major Version
     * * Value: ReleaseData as a List
     */
    val releaseListGroupMap: Map<Int, List<ReleaseData>> by lazy {
        releaseListSortedSet.groupBy { it.versionSemantic.major }
    }

    /**
     * [releaseList] mapped to [ReleaseData.version], sorted By [ReleaseData.versionSemantic]
     *
     * Generated from [releaseListSortedSet] - Keys and Lists should be sorted ascending, following Semantic Versioning Order
     *
     * * Key: Semantic Version String
     * * Value: ReleaseData
     */
    val releaseMapByVersionString: Map<String, ReleaseData> by lazy {
        releaseListSortedSet.associateBy { it.version }
    }

    /**
     * [releaseList] mapped to, and sorted by, [ReleaseData.versionSemantic]
     *
     * Generated from [releaseListSortedSet] - Keys and Lists should be sorted ascending, following Semantic Versioning Order
     *
     * * Key: Semantic Version Object
     * * Value: ReleaseData
     */
    val releaseMapBySemVer: Map<SemVer, ReleaseData> by lazy {
        releaseListSortedSet.associateBy { it.versionSemantic }
    }
}

@Serializable
data class ReleaseData(
    /**
     * Version of this Release
     *
     * Should follow [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
     */
    @SerialName("ver") val version: String,
    /**
     * URL to download Page for this Version
     *
     * e.g. "https://github.com/Author/project/releases/latest"
     */
    @SerialName("url") val pageUrl: String,
    /**
     * Direct Download URL for the External Java Version of this release
     *
     * Version that runs using an installed copy of Java 8
     *
     * e.g. "https://github.com/Author/project/download/version/plugin-externalJava.tpp"
     */
    @SerialName("urlDlExternal") val downloadExternalUrl: String? = null,
    /**
     * Direct Download URL for the Embedded Java Version of this release
     *
     * Version that runs using the Embedded version of Java in Touch Portal
     *
     * e.g. "https://github.com/Author/project/download/version/plugin-embeddedJava.tpp"
     */
    @SerialName("urlDlEmbedded") val downloadEmbeddedUrl: String? = null,
    /**
     * Recommended Next Release
     *
     * _Optional_
     *
     * Only set if plugin should be updated to an intermediate version before updating to the latest.
     *
     * If null, use [ReleaseBranchData.recommendedRelease]
     */
    @SerialName("recommendedNextVer") val recommendedNextRelease: String? = null,
    /**
     * Recommended Next Release Requires Manual Update
     *
     * _Optional_
     *
     * Only set if plugin needs to be updated manually to next version (e.g. breaking changes)
     *
     * If null, use [ReleaseBranchData.recommendedRelease]
     */
    @SerialName("recommendedNextVerManual") val recommendedNextReleaseRequiresManualUpdate: Boolean = false,

    ) : Comparable<ReleaseData> {
    /**
     * Version of this Release as a [SemVer]
     *
     * Lazily created. [version] must be a valid [Semantic Version](https://semver.org/spec/v2.0.0.html) or an error will be thrown
     */
    val versionSemantic: SemVer by lazy { SemVer.parse(version) }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     *
     * First compares the [version] strings, then [versionSemantic] if the strings are not equal
     */
    override fun compareTo(other: ReleaseData): Int {
        return if (this.version == other.version) 0
        else this.versionSemantic.compareTo(other.versionSemantic)
    }
}
