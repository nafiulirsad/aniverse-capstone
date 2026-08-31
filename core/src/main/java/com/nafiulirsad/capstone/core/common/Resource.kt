package com.nafiulirsad.capstone.core.common

/**
 * Wrapper that carries the state of a single-source-of-truth stream.
 *
 * It intentionally lives outside of both `data` and `domain` so that the domain layer never has
 * to depend on anything owned by the data layer.
 */
sealed class Resource<out T>(val data: T? = null, val message: String? = null) {
    class Loading<out T>(data: T? = null) : Resource<T>(data)

    class Success<out T>(data: T) : Resource<T>(data)

    class Error<out T>(message: String, data: T? = null) : Resource<T>(data, message)
}
