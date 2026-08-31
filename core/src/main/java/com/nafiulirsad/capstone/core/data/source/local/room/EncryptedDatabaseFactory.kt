package com.nafiulirsad.capstone.core.data.source.local.room

import android.content.Context
import androidx.room.Room
import com.nafiulirsad.capstone.core.data.source.local.security.DatabasePassphraseProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Builds the Room database on top of **SQLCipher** instead of the platform SQLite.
 *
 * Every page of `aniverse_encrypted.db` is written AES-256 encrypted, so pulling the file off a
 * rooted device (or out of an `adb backup`) yields ciphertext. The passphrase comes from
 * [DatabasePassphraseProvider], which keeps it sealed inside the Android Keystore.
 */
object EncryptedDatabaseFactory {

    fun create(context: Context, passphraseProvider: DatabasePassphraseProvider): AnimeDatabase {
        System.loadLibrary(SQLCIPHER_LIBRARY)
        deleteLegacyPlainTextDatabase(context)

        return Room.databaseBuilder(
            context,
            AnimeDatabase::class.java,
            AnimeDatabase.DATABASE_NAME,
        )
            .openHelperFactory(SupportOpenHelperFactory(passphraseProvider.passphrase()))
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    /**
     * Version 1.0 stored the same cache in an unencrypted file. Leaving it on disk would keep the
     * plain-text copy readable long after the app switched to SQLCipher, so it is removed on the
     * first run of an upgraded install (this also deletes its `-wal` and `-shm` files).
     */
    private fun deleteLegacyPlainTextDatabase(context: Context) {
        context.deleteDatabase(LEGACY_DATABASE_NAME)
    }

    private const val SQLCIPHER_LIBRARY = "sqlcipher"
    private const val LEGACY_DATABASE_NAME = "aniverse.db"
}
