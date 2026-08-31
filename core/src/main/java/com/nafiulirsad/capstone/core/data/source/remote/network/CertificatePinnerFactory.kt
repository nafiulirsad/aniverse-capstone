package com.nafiulirsad.capstone.core.data.source.remote.network

import okhttp3.CertificatePinner

/**
 * Certificate pinning for the Kitsu API.
 *
 * OkHttp still performs the normal chain validation; the pin is an extra requirement that at least
 * one certificate in the verified chain must match one of the SHA-256 public-key hashes below.
 * A man-in-the-middle proxy (Charles, mitmproxy, a rogue CA pushed onto the device) therefore
 * fails the handshake instead of reading the traffic.
 *
 * The pins target the **intermediate** and the **root** of the chain, not the leaf: Google Trust
 * Services rotates the `kitsu.io` leaf every ~90 days, so pinning the leaf would brick the app on
 * the next renewal.
 *
 *  - `kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=` - GTS `WE1` intermediate (valid to 2029-02-20)
 *  - `mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=` - `GTS Root R4` (valid to 2028-01-28), backup
 */
object CertificatePinnerFactory {

    fun create(): CertificatePinner = CertificatePinner.Builder()
        .add(KITSU_HOSTNAME, PIN_INTERMEDIATE_GTS_WE1, PIN_ROOT_GTS_R4)
        .build()

    const val KITSU_HOSTNAME = "kitsu.io"

    private const val PIN_INTERMEDIATE_GTS_WE1 =
        "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4="
    private const val PIN_ROOT_GTS_R4 =
        "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="
}
