package com.nafiulirsad.capstone.core.data.mapper

import com.nafiulirsad.capstone.core.data.source.local.entity.AnimeEntity
import com.nafiulirsad.capstone.core.data.source.local.entity.FavoriteAnimeEntity
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeDataResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeDetailResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeListResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.IncludedResponse
import com.nafiulirsad.capstone.core.domain.model.Anime

private const val UNKNOWN_TITLE = "Tanpa Judul"
private const val CATEGORY_TYPE = "categories"
private const val YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v="
private const val RATING_SCALE = 10.0
private const val YEAR_LENGTH = 4

fun AnimeListResponse.toEntities(): List<AnimeEntity> {
    val categories = included.toCategoryTitles()
    return data.orEmpty().mapNotNull { it.toEntityOrNull(categories) }
}

fun AnimeListResponse.toDomainList(): List<Anime> = toEntities().map { it.toDomain() }

fun AnimeDetailResponse.toDomainOrNull(): Anime? =
    data?.toEntityOrNull(included.toCategoryTitles())?.toDomain()

/** JSON:API keeps the related resources in a flat `included` array, keyed by id. */
private fun List<IncludedResponse>?.toCategoryTitles(): Map<String, String> =
    orEmpty()
        .filter { it.type == CATEGORY_TYPE }
        .mapNotNull { included ->
            val id = included.id ?: return@mapNotNull null
            val title = included.attributes?.title ?: return@mapNotNull null
            id to title
        }
        .toMap()

private fun AnimeDataResponse.toEntityOrNull(categories: Map<String, String>): AnimeEntity? {
    val animeId = id?.toIntOrNull() ?: return null
    val attributes = attributes ?: return null

    val poster = attributes.posterImage
    val cover = attributes.coverImage
    val posterUrl = poster?.medium ?: poster?.small ?: poster?.original.orEmpty()

    return AnimeEntity(
        animeId = animeId,
        title = attributes.canonicalTitle ?: attributes.titles?.english ?: UNKNOWN_TITLE,
        englishTitle = attributes.titles?.english ?: attributes.titles?.romaji,
        imageUrl = posterUrl,
        largeImageUrl = cover?.original ?: cover?.large ?: poster?.original ?: posterUrl,
        type = attributes.subtype,
        episodes = attributes.episodeCount,
        status = attributes.status,
        score = attributes.averageRating?.toDoubleOrNull()?.div(RATING_SCALE),
        ranking = attributes.ratingRank,
        members = attributes.userCount,
        year = attributes.startDate?.take(YEAR_LENGTH)?.toIntOrNull(),
        episodeMinutes = attributes.episodeLength,
        ageRating = attributes.ageRatingGuide,
        synopsis = attributes.synopsis ?: attributes.description,
        genres = relationships?.categories?.data
            ?.mapNotNull { categories[it.id] }
            .orEmpty(),
        trailerUrl = attributes.youtubeVideoId
            ?.takeIf { it.isNotBlank() }
            ?.let { YOUTUBE_WATCH_URL + it },
    )
}

fun AnimeEntity.toDomain(): Anime = Anime(
    animeId = animeId,
    title = title,
    englishTitle = englishTitle,
    imageUrl = imageUrl,
    largeImageUrl = largeImageUrl,
    type = type,
    episodes = episodes,
    status = status,
    score = score,
    ranking = ranking,
    members = members,
    year = year,
    episodeMinutes = episodeMinutes,
    ageRating = ageRating,
    synopsis = synopsis,
    genres = genres,
    trailerUrl = trailerUrl,
)

fun FavoriteAnimeEntity.toDomain(): Anime = Anime(
    animeId = animeId,
    title = title,
    englishTitle = englishTitle,
    imageUrl = imageUrl,
    largeImageUrl = largeImageUrl,
    type = type,
    episodes = episodes,
    status = status,
    score = score,
    ranking = ranking,
    members = members,
    year = year,
    episodeMinutes = episodeMinutes,
    ageRating = ageRating,
    synopsis = synopsis,
    genres = genres,
    trailerUrl = trailerUrl,
)

fun Anime.toFavoriteEntity(favoritedAt: Long): FavoriteAnimeEntity = FavoriteAnimeEntity(
    animeId = animeId,
    title = title,
    englishTitle = englishTitle,
    imageUrl = imageUrl,
    largeImageUrl = largeImageUrl,
    type = type,
    episodes = episodes,
    status = status,
    score = score,
    ranking = ranking,
    members = members,
    year = year,
    episodeMinutes = episodeMinutes,
    ageRating = ageRating,
    synopsis = synopsis,
    genres = genres,
    trailerUrl = trailerUrl,
    favoritedAt = favoritedAt,
)
