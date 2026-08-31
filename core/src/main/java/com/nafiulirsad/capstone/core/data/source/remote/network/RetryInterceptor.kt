package com.nafiulirsad.capstone.core.data.source.remote.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Kitsu is a free community API that can answer 502/504 for a few seconds at a time.
 * A short bounded retry keeps a transient gateway hiccup from surfacing as a failed screen.
 * Rate limiting (429) is deliberately not retried - hammering it would only make things worse.
 */
class RetryInterceptor(private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var lastFailure: IOException? = null

        for (attempt in 0 until maxAttempts) {
            if (attempt > 0) Thread.sleep(BACKOFF_MS * attempt)

            try {
                val response = chain.proceed(chain.request())
                val isLastAttempt = attempt == maxAttempts - 1
                if (response.code !in RETRYABLE_CODES || isLastAttempt) return response
                response.close()
            } catch (failure: IOException) {
                lastFailure = failure
            }
        }

        throw lastFailure ?: IOException("Server tidak merespons setelah $maxAttempts percobaan.")
    }

    private companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val BACKOFF_MS = 600L
        val RETRYABLE_CODES = setOf(500, 502, 503, 504)
    }
}
