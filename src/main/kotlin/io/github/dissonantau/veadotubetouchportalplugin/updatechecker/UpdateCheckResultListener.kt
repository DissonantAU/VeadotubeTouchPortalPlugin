package io.github.dissonantau.veadotubetouchportalplugin.updatechecker


import java.lang.Exception


interface UpdateCheckResultListener {
    /**
     * Process Received Update Check Result
     */
    fun onUpdateCheckResult(result: UpdateCheckResult)

    /**
     * Process Received Update Check Error
     */
    fun onUpdateCheckError(exception: Exception)
}