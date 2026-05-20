@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.dissonantau.veadotubetouchportalplugin.notification

import com.christophecvb.touchportal.TouchPortalPlugin
import com.christophecvb.touchportal.model.TPNotificationOptionClickedMessage
import io.github.dissonantau.veadotubetouchportalplugin.BuildConfig
import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPluginConstants
import io.github.dissonantau.veadotubetouchportalplugin.updatechecker.ReleaseData
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.net.URI

object NotificationHelper {
    /** Class Logger */
    @JvmStatic
    private val LOGGER =
        KotlinLogging.logger { TouchPortalPlugin::class.java.name }

    const val NOTIFICATION_BASE_ID = "${VeadoTouchPluginConstants.ID}.notification"
    const val NOTIFICATION_UPDATE_ID = "$NOTIFICATION_BASE_ID.update"


    enum class UpdateIDs(val id: String) {
        MAIN("$NOTIFICATION_UPDATE_ID.main"),
        DEV("$NOTIFICATION_UPDATE_ID.dev"),
        BOTH("$NOTIFICATION_UPDATE_ID.both")
    }

    enum class UpdateOptionIDs(val id: String) {
        MAIN_DOWNLOAD_BROWSER("$NOTIFICATION_UPDATE_ID.option.mainDownloadInBrowser"),
        MAIN_DOWNLOAD_COPY_LINK("$NOTIFICATION_UPDATE_ID.option.mainDownloadCopyLink"),
        MAIN_PAGE_BROWSER("$NOTIFICATION_UPDATE_ID.option.mainPageInBrowser"),
        MAIN_PAGE_COPY_LINK("$NOTIFICATION_UPDATE_ID.option.mainPageCopyLink"),

        DEV_DOWNLOAD_BROWSER("$NOTIFICATION_UPDATE_ID.option.devDownloadInBrowser"),
        DEV_DOWNLOAD_COPY_LINK("$NOTIFICATION_UPDATE_ID.option.devDownloadCopyLink"),
        DEV_PAGE_BROWSER("$NOTIFICATION_UPDATE_ID.option.devPageInBrowser"),
        DEV_PAGE_COPY_LINK("$NOTIFICATION_UPDATE_ID.option.devPageCopyLink")
    }


    /**
     * Opens URI (if supported by OS)
     *
     * @param openURI URI to Open
     * @return `true` if Browse command is run, `false` if opening URIs are not supported.
     *
     * True is not a guarantee that the page opened, just that the command was reported as supported and there wasn't an exception.
     *
     * @see URI
     * @see Desktop.getDesktop
     * @see Desktop.browse
     */
    @JvmStatic
    fun openUpdateLink(openURI: URI): Boolean {
        LOGGER.debug { "openUpdateLink: Open ${openURI.toASCIIString()} in Default Browser" }
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(openURI)
                LOGGER.debug { "openUpdateLink: Invoked desktop.browse" }
                return true
            } else
                LOGGER.debug { "openUpdateLink: Desktop Browse Action is not supported" }
        } else
            LOGGER.debug { "openUpdateLink: Desktop is not supported" }

        return false
    }

    /**
     * Copy Text to System Clipboard
     *
     * @param text String to copy to Clipboard
     * @see Toolkit.getDefaultToolkit
     * @see Toolkit.getSystemClipboard
     * @see Clipboard.setContents
     */
    @JvmStatic
    fun copyTextToClipboard(text: String) {
        LOGGER.debug { "copyTextToClipboard: Copy '$text' to clipboard" }
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val strSelection = StringSelection(text)
        clipboard.setContents(strSelection, strSelection)
    }


    fun respondNotificationClickedUpdateBoth(
        tpNotificationOptionClickedMessage: TPNotificationOptionClickedMessage,
        versionInfoMain: ReleaseData, versionInfo: ReleaseData
    ) {
        when (tpNotificationOptionClickedMessage.optionId) {
            UpdateOptionIDs.MAIN_DOWNLOAD_BROWSER.id -> {
                val dlUrl = if (BuildConfig.USES_TP_BUNDLED_JRE && versionInfoMain.downloadUrlBundled != null)
                    versionInfoMain.urlDownloadBundled else versionInfoMain.urlDownloadExternal

                if (dlUrl == null) {
                    LOGGER.debug { "onNotificationOptionClicked - Open Download Button Pressed, but Download URL is missing. Opening Download Page URL instead" }
                    openUpdateLink(versionInfoMain.urlDownloadPage.toURI())
                } else {
                    LOGGER.debug { "onNotificationOptionClicked - Open Download Button Pressed, opening '${dlUrl}'" }
                    openUpdateLink(dlUrl.toURI())
                }

            }

            UpdateOptionIDs.MAIN_DOWNLOAD_COPY_LINK.id -> {
                val dlUrl = if (BuildConfig.USES_TP_BUNDLED_JRE && versionInfoMain.downloadUrlBundled != null)
                    versionInfoMain.urlDownloadBundled else versionInfoMain.urlDownloadExternal

                if (dlUrl == null) {
                    LOGGER.debug { "onNotificationOptionClicked - Copy Download Button Pressed, but Download URL is missing. copying Download Page URL instead" }
                    copyTextToClipboard(versionInfoMain.urlDownloadPage.toString())
                } else {
                    LOGGER.debug { "onNotificationOptionClicked - Copy Download Button Pressed, copying to clipboard: '${dlUrl}'" }
                    copyTextToClipboard(dlUrl.toString())
                }
            }

            UpdateOptionIDs.MAIN_PAGE_BROWSER.id -> {
                LOGGER.debug { "onNotificationOptionClicked - Open Page Button Pressed, opening page: '${versionInfoMain.urlDownloadPage}'" }
                copyTextToClipboard(versionInfoMain.urlDownloadPage.toString())
            }

            UpdateOptionIDs.MAIN_PAGE_COPY_LINK.id -> {
                LOGGER.debug { "onNotificationOptionClicked - Copy Page Button Pressed, copying to clipboard: '${versionInfoMain.urlDownloadPage}'" }
                copyTextToClipboard(versionInfoMain.urlDownloadPage.toString())
            }

            UpdateOptionIDs.DEV_DOWNLOAD_BROWSER.id -> {
                val dlUrl = if (BuildConfig.USES_TP_BUNDLED_JRE && versionInfo.downloadUrlBundled != null)
                    versionInfo.urlDownloadBundled
                else
                    versionInfo.urlDownloadExternal

                if (dlUrl == null) {
                    LOGGER.debug { "onNotificationOptionClicked - Open Download Button Pressed, but Download URL is missing. Opening Download Page URL instead" }
                    openUpdateLink(versionInfo.urlDownloadPage.toURI())
                } else {
                    LOGGER.debug { "onNotificationOptionClicked - Open Download Button Pressed, opening '${dlUrl}'" }
                    openUpdateLink(dlUrl.toURI())
                }
            }

            UpdateOptionIDs.DEV_DOWNLOAD_COPY_LINK.id -> {
                val dlUrl = if (BuildConfig.USES_TP_BUNDLED_JRE && versionInfo.downloadUrlBundled != null)
                    versionInfo.urlDownloadBundled else versionInfo.urlDownloadExternal

                if (dlUrl == null) {
                    LOGGER.debug { "onNotificationOptionClicked - Copy Download Button Pressed, but Download URL is missing. copying Download Page URL instead" }
                    copyTextToClipboard(versionInfo.urlDownloadPage.toString())
                } else {
                    LOGGER.debug { "onNotificationOptionClicked - Copy Download Button Pressed, copying to clipboard: '${dlUrl}'" }
                    copyTextToClipboard(dlUrl.toString())
                }
            }

            UpdateOptionIDs.DEV_PAGE_BROWSER.id -> {
                LOGGER.debug { "onNotificationOptionClicked - Open Page Button Pressed, opening page: '${versionInfo.urlDownloadPage}'" }
                copyTextToClipboard(versionInfo.urlDownloadPage.toString())
            }

            UpdateOptionIDs.DEV_PAGE_COPY_LINK.id -> {
                LOGGER.debug { "onNotificationOptionClicked - Copy Page Button Pressed, copying to clipboard: '${versionInfo.urlDownloadPage}'" }
                copyTextToClipboard(versionInfo.urlDownloadPage.toString())
            }

            else -> LOGGER.warn { "Unknown Notification Option ID Received" }
        }
    }

    fun respondNotificationClickedUpdateDev(
        tpNotificationOptionClickedMessage: TPNotificationOptionClickedMessage, versionInfo: ReleaseData
    ) {
        when (tpNotificationOptionClickedMessage.optionId) {
            UpdateOptionIDs.DEV_DOWNLOAD_BROWSER.id -> {
                val dlUrl = if (BuildConfig.USES_TP_BUNDLED_JRE && versionInfo.downloadUrlBundled != null)
                    versionInfo.urlDownloadBundled else versionInfo.urlDownloadExternal

                if (dlUrl == null) {
                    LOGGER.debug { "onNotificationOptionClicked - Open Download Button Pressed, but Download URL is missing. Opening Download Page URL instead" }
                    openUpdateLink(versionInfo.urlDownloadPage.toURI())
                } else {
                    LOGGER.debug { "onNotificationOptionClicked - Open Download Button Pressed, opening '${dlUrl}'" }
                    openUpdateLink(dlUrl.toURI())
                }
            }

            UpdateOptionIDs.DEV_DOWNLOAD_COPY_LINK.id -> {
                val dlUrl = if (BuildConfig.USES_TP_BUNDLED_JRE && versionInfo.downloadUrlBundled != null)
                    versionInfo.urlDownloadBundled else versionInfo.urlDownloadExternal

                if (dlUrl == null) {
                    LOGGER.debug { "onNotificationOptionClicked - Copy Download Button Pressed, but Download URL is missing. copying Download Page URL instead" }
                    copyTextToClipboard(versionInfo.urlDownloadPage.toString())
                } else {
                    LOGGER.debug { "onNotificationOptionClicked - Copy Download Button Pressed, copying to clipboard: '${dlUrl}'" }
                    copyTextToClipboard(dlUrl.toString())
                }

            }

            UpdateOptionIDs.DEV_PAGE_BROWSER.id -> {
                LOGGER.debug { "onNotificationOptionClicked - Open Page Button Pressed, opening page: '${versionInfo.urlDownloadPage}'" }
                copyTextToClipboard(versionInfo.urlDownloadPage.toString())
            }

            UpdateOptionIDs.DEV_PAGE_COPY_LINK.id -> {
                LOGGER.debug { "onNotificationOptionClicked - Copy Page Button Pressed, copying to clipboard: '${versionInfo.urlDownloadPage}'" }
                copyTextToClipboard(versionInfo.urlDownloadPage.toString())
            }

            else -> LOGGER.warn { "Unknown Notification Option ID Received" }
        }
    }

    fun respondNotificationClickedUpdateMain(
        tpNotificationOptionClickedMessage: TPNotificationOptionClickedMessage, versionInfo: ReleaseData
    ) {

        when (tpNotificationOptionClickedMessage.optionId) {
            UpdateOptionIDs.MAIN_DOWNLOAD_BROWSER.id -> {
                val dlUrl =
                    if (BuildConfig.USES_TP_BUNDLED_JRE && versionInfo.downloadUrlBundled != null)
                        versionInfo.urlDownloadBundled
                    else
                        versionInfo.urlDownloadExternal

                if (dlUrl == null) {
                    LOGGER.debug { "onNotificationOptionClicked - Open Download Button Pressed, but Download URL is missing. Opening Download Page URL instead" }
                    openUpdateLink(versionInfo.urlDownloadPage.toURI())
                } else {
                    LOGGER.debug { "onNotificationOptionClicked - Open Download Button Pressed, opening '${dlUrl}'" }
                    openUpdateLink(dlUrl.toURI())
                }

            }
            UpdateOptionIDs.MAIN_DOWNLOAD_COPY_LINK.id -> {
                val dlUrl =
                    if (BuildConfig.USES_TP_BUNDLED_JRE && versionInfo.downloadUrlBundled != null)
                        versionInfo.urlDownloadBundled
                    else
                        versionInfo.urlDownloadExternal

                if (dlUrl == null) {
                    LOGGER.debug { "onNotificationOptionClicked - Copy Download Button Pressed, but Download URL is missing. copying Download Page URL instead" }
                    copyTextToClipboard(versionInfo.urlDownloadPage.toString())
                } else {
                    LOGGER.debug { "onNotificationOptionClicked - Copy Download Button Pressed, copying to clipboard: '${dlUrl}'" }
                    copyTextToClipboard(dlUrl.toString())
                }
            }
            UpdateOptionIDs.MAIN_PAGE_BROWSER.id -> {
                LOGGER.debug { "onNotificationOptionClicked - Open Page Button Pressed, opening page: '${versionInfo.urlDownloadPage}'" }
                copyTextToClipboard(versionInfo.urlDownloadPage.toString())
            }
            UpdateOptionIDs.MAIN_PAGE_COPY_LINK.id -> {
                LOGGER.debug { "onNotificationOptionClicked - Copy Page Button Pressed, copying to clipboard: '${versionInfo.urlDownloadPage}'" }
                copyTextToClipboard(versionInfo.urlDownloadPage.toString())
            }
            else -> LOGGER.warn { "Unknown Notification Option ID Received" }
        }
    }

}