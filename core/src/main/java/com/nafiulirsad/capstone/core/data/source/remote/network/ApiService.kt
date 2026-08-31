package com.nafiulirsad.capstone.core.data.source.remote.network

import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeDetailResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeListResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/** Kitsu (https://kitsu.docs.apiary.io) - a public anime catalogue that needs no API key. */
interface ApiService {

    @Headers(ACCEPT_JSON_API)
    @GET("anime")
    suspend fun getTopAnime(
        @Query("page[limit]") limit: Int,
        @Query("sort") sort: String,
    ): AnimeListResponse

    @Headers(ACCEPT_JSON_API)
    @GET("anime")
    suspend fun searchAnime(
        @Query("filter[text]") query: String,
        @Query("page[limit]") limit: Int,
    ): AnimeListResponse

    @Headers(ACCEPT_JSON_API)
    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") animeId: Int,
        @Query("include") include: String,
    ): AnimeDetailResponse

    companion object {
        const val ACCEPT_JSON_API = "Accept: application/vnd.api+json"
    }
}
