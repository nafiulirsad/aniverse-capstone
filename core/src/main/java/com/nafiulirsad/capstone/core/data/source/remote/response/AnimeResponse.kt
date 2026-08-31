package com.nafiulirsad.capstone.core.data.source.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Data-layer models. Kitsu speaks JSON:API, so a payload is split into `data` (the resources) and
 * `included` (the resources pulled in through `?include=`), linked together by `relationships`.
 */
data class AnimeListResponse(
    @SerializedName("data")
    val data: List<AnimeDataResponse>? = null,
    @SerializedName("included")
    val included: List<IncludedResponse>? = null,
)

data class AnimeDetailResponse(
    @SerializedName("data")
    val data: AnimeDataResponse? = null,
    @SerializedName("included")
    val included: List<IncludedResponse>? = null,
)

data class AnimeDataResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("attributes")
    val attributes: AnimeAttributesResponse? = null,
    @SerializedName("relationships")
    val relationships: RelationshipsResponse? = null,
)

data class AnimeAttributesResponse(
    @SerializedName("canonicalTitle")
    val canonicalTitle: String? = null,
    @SerializedName("titles")
    val titles: TitlesResponse? = null,
    @SerializedName("synopsis")
    val synopsis: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("averageRating")
    val averageRating: String? = null,
    @SerializedName("ratingRank")
    val ratingRank: Int? = null,
    @SerializedName("userCount")
    val userCount: Int? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("subtype")
    val subtype: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("posterImage")
    val posterImage: ImageResponse? = null,
    @SerializedName("coverImage")
    val coverImage: ImageResponse? = null,
    @SerializedName("episodeCount")
    val episodeCount: Int? = null,
    @SerializedName("episodeLength")
    val episodeLength: Int? = null,
    @SerializedName("ageRatingGuide")
    val ageRatingGuide: String? = null,
    @SerializedName("youtubeVideoId")
    val youtubeVideoId: String? = null,
)

data class TitlesResponse(
    @SerializedName("en")
    val english: String? = null,
    @SerializedName("en_jp")
    val romaji: String? = null,
)

data class ImageResponse(
    @SerializedName("small")
    val small: String? = null,
    @SerializedName("medium")
    val medium: String? = null,
    @SerializedName("large")
    val large: String? = null,
    @SerializedName("original")
    val original: String? = null,
)

data class RelationshipsResponse(
    @SerializedName("categories")
    val categories: RelationshipResponse? = null,
)

data class RelationshipResponse(
    @SerializedName("data")
    val data: List<ResourceIdentifierResponse>? = null,
)

data class ResourceIdentifierResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
)

data class IncludedResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("attributes")
    val attributes: IncludedAttributesResponse? = null,
)

data class IncludedAttributesResponse(
    @SerializedName("title")
    val title: String? = null,
)
