package com.nafiulirsad.capstone.core.data.source.remote.network

/** Raw outcome of one network call, before it is turned into a [com.nafiulirsad.capstone.core.common.Resource]. */
sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()

    data class Error(val message: String) : ApiResponse<Nothing>()

    data object Empty : ApiResponse<Nothing>()
}
