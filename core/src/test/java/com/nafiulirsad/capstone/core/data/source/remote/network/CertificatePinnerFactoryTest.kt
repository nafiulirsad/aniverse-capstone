package com.nafiulirsad.capstone.core.data.source.remote.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the pinning configuration itself: a pin that silently disappears would leave the app
 * shipping without the protection the release notes claim it has.
 */
class CertificatePinnerFactoryTest {

    private val certificatePinner = CertificatePinnerFactory.create()

    @Test
    fun `the api host is pinned to two keys, so one rotation cannot lock the app out`() {
        val pins = certificatePinner.pins.filter { it.pattern == CertificatePinnerFactory.KITSU_HOSTNAME }

        assertEquals(2, pins.size)
        assertTrue(pins.all { it.hashAlgorithm == "sha256" })
    }

    @Test
    fun `no other host is pinned by accident`() {
        val patterns = certificatePinner.pins.map { it.pattern }.toSet()

        assertEquals(setOf(CertificatePinnerFactory.KITSU_HOSTNAME), patterns)
    }
}
