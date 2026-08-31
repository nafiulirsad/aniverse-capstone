package com.nafiulirsad.capstone.core.data

import com.nafiulirsad.capstone.core.common.Resource
import com.nafiulirsad.capstone.core.data.source.remote.network.ApiResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkBoundResourceTest {

    @Test
    fun `fresh data is written to the cache and then emitted from it`() = runTest {
        val cache = MutableStateFlow(emptyList<String>())

        val emissions = networkBoundResource(
            query = { cache },
            fetch = { flowOf(ApiResponse.Success(listOf("remote"))) },
            saveFetchResult = { result -> cache.value = result },
            shouldFetch = { it.isEmpty() },
        ).take(3).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Loading)
        assertTrue(emissions[2] is Resource.Success)
        assertEquals(listOf("remote"), emissions[2].data)
    }

    @Test
    fun `a failed refresh still serves the cached data`() = runTest {
        val cache = MutableStateFlow(listOf("cached"))

        val emissions = networkBoundResource(
            query = { cache },
            fetch = { flowOf(ApiResponse.Error("Tidak ada koneksi internet.")) },
            saveFetchResult = { },
            shouldFetch = { true },
        ).take(3).toList()

        val last = emissions.last()
        assertTrue(last is Resource.Error)
        assertEquals(listOf("cached"), last.data)
        assertEquals("Tidak ada koneksi internet.", last.message)
    }

    @Test
    fun `the network is skipped when the cache is considered fresh`() = runTest {
        val cache = MutableStateFlow(listOf("cached"))
        var fetched = false

        val emissions = networkBoundResource(
            query = { cache },
            fetch = {
                fetched = true
                flowOf(ApiResponse.Empty)
            },
            saveFetchResult = { },
            shouldFetch = { false },
        ).take(2).toList()

        assertTrue(!fetched)
        assertTrue(emissions[1] is Resource.Success)
        assertEquals(listOf("cached"), emissions[1].data)
    }
}
