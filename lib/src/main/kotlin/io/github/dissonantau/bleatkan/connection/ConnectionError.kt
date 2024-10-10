package io.github.dissonantau.bleatkan.connection


enum class ConnectionError {
    None,
    InvalidServerOrName,
    FailedToConnect,
    ExceededRetries
}
