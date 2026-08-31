package com.nafiulirsad.capstone.core.data.source.remote.network

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RetryInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        client = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(maxAttempts = MAX_ATTEMPTS))
            .build()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `a gateway error is retried until the server answers`() {
        server.enqueue(MockResponse.Builder().code(HTTP_BAD_GATEWAY).build())
        server.enqueue(MockResponse.Builder().code(HTTP_OK).body("{}").build())

        val response = client.newCall(Request.Builder().url(server.url("/anime")).build()).execute()

        assertEquals(HTTP_OK, response.code)
        assertEquals(2, server.requestCount)
        response.close()
    }

    @Test
    fun `rate limiting is returned to the caller instead of being hammered`() {
        server.enqueue(MockResponse.Builder().code(HTTP_TOO_MANY_REQUESTS).build())

        val response = client.newCall(Request.Builder().url(server.url("/anime")).build()).execute()

        assertEquals(HTTP_TOO_MANY_REQUESTS, response.code)
        assertEquals(1, server.requestCount)
        response.close()
    }

    @Test
    fun `a server that never recovers gives up after the last attempt`() {
        repeat(MAX_ATTEMPTS) {
            server.enqueue(MockResponse.Builder().code(HTTP_SERVICE_UNAVAILABLE).build())
        }

        val response = client.newCall(Request.Builder().url(server.url("/anime")).build()).execute()

        assertEquals(HTTP_SERVICE_UNAVAILABLE, response.code)
        assertEquals(MAX_ATTEMPTS, server.requestCount)
        response.close()
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
        const val HTTP_OK = 200
        const val HTTP_BAD_GATEWAY = 502
        const val HTTP_SERVICE_UNAVAILABLE = 503
        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}
