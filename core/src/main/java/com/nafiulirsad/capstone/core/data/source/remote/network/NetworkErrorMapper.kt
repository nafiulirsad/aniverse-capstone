package com.nafiulirsad.capstone.core.data.source.remote.network

import android.content.Context
import com.nafiulirsad.capstone.core.R
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Turns a low-level network failure into a message the user can actually act on.
 * Injected instead of hardcoded so the strings stay localizable and the data source stays testable.
 */
class NetworkErrorMapper(private val context: Context) {

    fun map(throwable: Throwable): String = when (throwable) {
        is SocketTimeoutException -> context.getString(R.string.error_timeout)

        is IOException -> context.getString(R.string.error_no_connection)

        is HttpException -> when (throwable.code()) {
            HTTP_TOO_MANY_REQUESTS -> context.getString(R.string.error_rate_limited)
            else -> context.getString(R.string.error_server, throwable.code())
        }

        else -> context.getString(R.string.error_unknown)
    }

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}
