package com.nafiulirsad.capstone.core.data.source.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nafiulirsad.capstone.core.data.source.local.entity.AnimeEntity
import com.nafiulirsad.capstone.core.data.source.local.entity.FavoriteAnimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime ORDER BY ranking ASC")
    fun getTopAnime(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE animeId = :animeId LIMIT 1")
    fun getCachedAnime(animeId: Int): Flow<AnimeEntity?>

    @Query(
        """
        SELECT * FROM anime
        WHERE title LIKE '%' || :query || '%' OR englishTitle LIKE '%' || :query || '%'
        ORDER BY ranking ASC
        """,
    )
    fun searchCachedAnime(query: String): Flow<List<AnimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopAnime(anime: List<AnimeEntity>)

    @Query("DELETE FROM anime")
    suspend fun clearTopAnime()

    @Transaction
    suspend fun replaceTopAnime(anime: List<AnimeEntity>) {
        clearTopAnime()
        insertTopAnime(anime)
    }

    @Query("SELECT * FROM favorite_anime ORDER BY favoritedAt DESC")
    fun getFavoriteAnime(): Flow<List<FavoriteAnimeEntity>>

    @Query("SELECT * FROM favorite_anime WHERE animeId = :animeId LIMIT 1")
    fun getFavoriteAnime(animeId: Int): Flow<FavoriteAnimeEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_anime WHERE animeId = :animeId)")
    fun isFavorite(animeId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(anime: FavoriteAnimeEntity)

    @Query("DELETE FROM favorite_anime WHERE animeId = :animeId")
    suspend fun deleteFavorite(animeId: Int)
}
