package io.github.dissonantau.veadotubetouchportalplugin.updatechecker


import java.lang.Exception


interface UpdateCheckResultListener {
    /**
     * Process Received Update Check Result
     */
    fun onUpdateCheckResult(updateData: UpdateReleaseData)

    /**
     * Process Received Update Check Error
     */
    fun onUpdateCheckError(exception: Exception)
}