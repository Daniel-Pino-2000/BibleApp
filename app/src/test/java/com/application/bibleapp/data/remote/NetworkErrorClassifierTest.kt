package com.application.bibleapp.data.remote

import kotlinx.serialization.SerializationException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

class NetworkErrorClassifierTest {

    @Test
    fun `IOException family is retriable`() {
        assertTrue(isRetriableNetworkError(UnknownHostException()))
        assertTrue(isRetriableNetworkError(SocketTimeoutException()))
        assertTrue(isRetriableNetworkError(ConnectException()))
        assertTrue(isRetriableNetworkError(IOException("connection reset")))
    }

    @Test
    fun `DNS resolution failure is retriable even though it does not extend IOException`() {
        // This is the exact exception from the field report: UnresolvedAddressException
        // extends IllegalArgumentException, not IOException — it needs its own branch.
        assertTrue(isRetriableNetworkError(UnresolvedAddressException()))
    }

    @Test
    fun `validation and parsing errors are not retried`() {
        assertFalse(isRetriableNetworkError(IllegalArgumentException("bad book slug")))
        assertFalse(isRetriableNetworkError(SerializationException("malformed json")))
    }
}
