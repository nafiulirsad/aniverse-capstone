package com.nafiulirsad.capstone.core.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nafiulirsad.capstone.core.data.source.local.entity.AnimeEntity
import com.nafiulirsad.capstone.core.data.source.local.entity.FavoriteAnimeEntity

@Database(
    entities = [AnimeEntity::class, FavoriteAnimeEntity::class],
    // Bumped when the Kitsu migration renamed columns; the destructive fallback then rebuilds
    // the cache instead of crashing on an identity-hash mismatch.
    version = 2,
    exportSchema = true,
)
@TypeConverters(StringListConverter::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao

    companion object {
        /**
         * A new file name on purpose: the pre-2.0 `aniverse.db` was plain text, and SQLCipher
         * cannot open a plain-text file. Starting fresh keeps an upgrading install from crashing.
         */
        const val DATABASE_NAME = "aniverse_encrypted.db"
    }
}
