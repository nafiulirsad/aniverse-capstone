package com.nafiulirsad.capstone.core.data

import com.nafiulirsad.capstone.core.data.mapper.toDomainList
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeAttributesResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeDataResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.AnimeListResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.ImageResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.IncludedAttributesResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.IncludedResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.RelationshipResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.RelationshipsResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.ResourceIdentifierResponse
import com.nafiulirsad.capstone.core.data.source.remote.response.TitlesResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeMapperTest {

    private val attributes = AnimeAttributesResponse(
        canonicalTitle = "Attack on Titan",
        titles = TitlesResponse(english = "Attack on Titan", romaji = "Shingeki no Kyojin"),
        synopsis = "Centuries ago, mankind was slaughtered.",
        averageRating = "84.45",
        ratingRank = 48,
        userCount = 614_149,
        startDate = "2013-04-07",
        subtype = "TV",
        status = "finished",
        posterImage = ImageResponse(medium = "https://cdn.test/medium.jpg"),
        coverImage = ImageResponse(original = "https://cdn.test/cover.jpg"),
        episodeCount = 25,
        episodeLength = 24,
        ageRatingGuide = "Violence, Profanity",
        youtubeVideoId = "LHtdKWJdif4",
    )

    private val payload = AnimeListResponse(
        data = listOf(
            AnimeDataResponse(
                id = "7442",
                attributes = attributes,
                relationships = RelationshipsResponse(
                    categories = RelationshipResponse(
                        data = listOf(
                            ResourceIdentifierResponse(id = "150", type = "categories"),
                            ResourceIdentifierResponse(id = "999", type = "categories"),
                        ),
                    ),
                ),
            ),
        ),
        included = listOf(
            IncludedResponse(
                id = "150",
                type = "categories",
                attributes = IncludedAttributesResponse(title = "Action"),
            ),
        ),
    )

    @Test
    fun `payload maps into the domain model`() {
        val anime = payload.toDomainList().single()

        assertEquals(7442, anime.animeId)
        assertEquals("Attack on Titan", anime.title)
        assertEquals("https://cdn.test/medium.jpg", anime.imageUrl)
        assertEquals("https://cdn.test/cover.jpg", anime.largeImageUrl)
        assertEquals(2013, anime.year)
        assertEquals("Violence, Profanity", anime.ageRating)
    }

    @Test
    fun `rating is rescaled from 100 to 10`() {
        val anime = payload.toDomainList().single()

        assertEquals(8.445, requireNotNull(anime.score), 0.0001)
    }

    @Test
    fun `only categories present in the included array become genres`() {
        val anime = payload.toDomainList().single()

        assertEquals(listOf("Action"), anime.genres)
    }

    @Test
    fun `the youtube id becomes a playable watch url`() {
        val anime = payload.toDomainList().single()

        assertEquals("https://www.youtube.com/watch?v=LHtdKWJdif4", anime.trailerUrl)
    }

    @Test
    fun `an entry without an id or attributes is dropped instead of crashing`() {
        val broken = AnimeListResponse(
            data = listOf(
                AnimeDataResponse(id = null, attributes = attributes),
                AnimeDataResponse(id = "1", attributes = null),
            ),
        )

        assertTrue(broken.toDomainList().isEmpty())
    }

    @Test
    fun `a missing poster falls back to an empty url instead of crashing`() {
        val withoutImages = AnimeListResponse(
            data = listOf(
                AnimeDataResponse(
                    id = "1",
                    attributes = attributes.copy(posterImage = null, coverImage = null),
                ),
            ),
        )

        val anime = withoutImages.toDomainList().single()
        assertTrue(anime.imageUrl.isEmpty())
        assertTrue(anime.largeImageUrl.isEmpty())
    }
}
