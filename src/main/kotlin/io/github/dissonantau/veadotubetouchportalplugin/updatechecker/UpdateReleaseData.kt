package io.github.dissonantau.veadotubetouchportalplugin.updatechecker

/**
 * Contains Release info from calculated
 */
class UpdateReleaseData {
    val updateAvailable: Boolean
        get() = mainBranchUpdateAvailable || devBranchUpdateAvailable

    var mainBranchUpdateAvailable = false
    var mainBranchReleaseData: ReleaseData? = null
    var mainBranchManualUpdateRequired = false

    var devBranchUpdateAvailable = false
    var devBranchReleaseData: ReleaseData? = null
    var devBranchManualUpdateRequired = false

}