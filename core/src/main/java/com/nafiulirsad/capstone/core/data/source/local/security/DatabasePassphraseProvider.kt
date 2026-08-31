package com.nafiulirsad.capstone.core.data.source.local.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Supplies the passphrase SQLCipher opens the encrypted Room database with.
 *
 * The passphrase itself is 32 random bytes generated once on the device. It is never stored in
 * plain text: it is sealed with an AES-256/GCM key that lives inside the **Android Keystore**,
 * so the raw key material never leaves the hardware-backed key store and cannot be read out of
 * the APK, out of a decompiled class, or out of a backup of the preferences file.
 */
class DatabasePassphraseProvider(private val context: Context, private val databaseName: String) {

    private val preferences by lazy {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * @return the passphrase for SQLCipher, decrypted in memory only.
     * A device whose Keystore entry became unusable (factory reset of the secure hardware,
     * restored backup) gets a fresh key and a fresh database instead of a crash loop.
     */
    fun passphrase(): ByteArray = try {
        readStoredPassphrase() ?: createAndStorePassphrase()
    } catch (failure: GeneralSecurityException) {
        Log.w(TAG, "Keystore entry is unusable, regenerating the database key", failure)
        resetKeyMaterial()
        createAndStorePassphrase()
    }

    private fun readStoredPassphrase(): ByteArray? {
        val stored = preferences.getString(KEY_PASSPHRASE, null) ?: return null
        val sealed = Base64.decode(stored, Base64.NO_WRAP)
        if (sealed.size <= IV_LENGTH) return null

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(GCM_TAG_BITS, sealed, 0, IV_LENGTH),
            )
        }
        return cipher.doFinal(sealed, IV_LENGTH, sealed.size - IV_LENGTH)
    }

    private fun createAndStorePassphrase(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_LENGTH).also(SecureRandom()::nextBytes)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey())
        }
        val sealed = cipher.iv + cipher.doFinal(passphrase)

        preferences.edit { putString(KEY_PASSPHRASE, Base64.encodeToString(sealed, Base64.NO_WRAP)) }

        return passphrase
    }

    /** Reuses the Keystore entry when it exists, and creates a hardware-backed one when it does not. */
    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val specification = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            .apply { init(specification) }
            .generateKey()
    }

    /** Drops the unusable key together with the database it can no longer open. */
    private fun resetKeyMaterial() {
        runCatching {
            KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }.deleteEntry(KEY_ALIAS)
        }
        preferences.edit { remove(KEY_PASSPHRASE) }
        context.deleteDatabase(databaseName)
    }

    private companion object {
        const val TAG = "DatabaseKey"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "aniverse_database_key"
        const val PREFERENCE_NAME = "aniverse_secure_store"
        const val KEY_PASSPHRASE = "database_passphrase"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val GCM_TAG_BITS = 128
        const val IV_LENGTH = 12
        const val PASSPHRASE_LENGTH = 32
    }
}
