package com.application.bibleapp.data.remote

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException as KtorSocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

/**
 * True for transient, retriable network failures (DNS resolution blips, connect/read
 * timeouts, connection resets) as opposed to permanent conditions (bad request params,
 * malformed response) that retrying won't fix.
 */
fun isRetriableNetworkError(e: Throwable): Boolean = when (e) {
    // Covers UnknownHostException, SocketException, java.net.SocketTimeoutException,
    // "connection reset by peer", etc.
    is IOException -> true
    // DNS resolution failure — notably does NOT extend IOException.
    is UnresolvedAddressException -> true
    is HttpRequestTimeoutException -> true
    is ConnectTimeoutException -> true
    is KtorSocketTimeoutException -> true
    else -> false
}
