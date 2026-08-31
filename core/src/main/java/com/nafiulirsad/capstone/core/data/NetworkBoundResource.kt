package com.nafiulirsad.capstone.core.data

import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth: the database always feeds the UI, the network only refills the database.
 */
fun <ResultType, RequestType> networkBoundResource(
    query: () -> Flow<ResultType>,
    fetch: () -> Flow<ApiResponse<RequestType>>,
    saveFetchResult: suspend (RequestType) -> Unit,
    shouldFetch: (ResultType) -> Boolean,
): Flow<Resource<ResultType>> = flow {
    emit(Resource.Loading())
    val cached = query().first()

    if (!shouldFetch(cached)) {
        emitAll(query().map { Resource.Success(it) })
        return@flow
    }

    emit(Resource.Loading(cached))
    when (val response = fetch().first()) {
        is ApiResponse.Success -> {
            saveFetchResult(response.data)
            emitAll(query().map { Resource.Success(it) })
        }

        is ApiResponse.Empty -> {
            emitAll(query().map { Resource.Success(it) })
        }

        is ApiResponse.Error -> {
            emitAll(query().map { Resource.Error(response.message, it) })
        }
    }
}
