package io.github.dissonantau.veadotubetouchportalplugin.updatechecker


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


class PluginUpdateChecker(private val listener: UpdateCheckResultListener, private val updateCheckReleasesUri: String) {

    private val localLogger =
        KotlinLogging.logger { PluginUpdateChecker::class.java.name }

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
                level = if (localLogger.isDebugEnabled()) LogLevel.INFO else LogLevel.NONE
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

            if (response.status != HttpStatusCode.OK){


            }


            val responseJSONText = response.bodyAsText()

            // Decode and Convert JSON to Object
            localLogger.trace { "runUpdateCheck: Attempting to decode JSON String to object:\n$responseJSONText" }

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

                    localLogger.debug { "runUpdateCheck: Unable to decode JSON String to UpdateCheckResult: ${thrown.message}\n$responseJSONText" }

                    try {
                        //Try to convert to generic JSON Element - this isn't passed, but will let us know if it's valid JSON
                        val jsonMessage = Json.parseToJsonElement(responseJSONText)
                        warningString.append("; Successfully decoded to Generic JSON Element")
                        localLogger.warn { "Warning - Failed to Deserialize JSON to Object: $jsonMessage" }

                    } catch (exInner: Exception) {
                        warningString.append("; Failed decode to Generic JSON Element")
                        localLogger.warn { "Warning - Failed to Deserialize JSON. Error: '${exInner.message}' > JSON: '$responseJSONText'" }
                        //Add as Suppressed Exception
                        thrown.addSuppressed(exInner)
                    }

                    val exception = Exception(warningString.toString(), thrown)
                    throw exception
                }


            // Send to Listener
            listener.onUpdateCheckResult(convertedMessage)

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