package com.nafiulirsad.capstone.core.data.source.local.room

import org.junit.Assert.assertEquals
import org.junit.Test

class StringListConverterTest {

    private val converter = StringListConverter()

    @Test
    fun `a genre list survives a round trip through the column`() {
        val genres = listOf("Action", "Comedy", "Slice of Life")

        assertEquals(genres, converter.toList(converter.fromList(genres)))
    }

    @Test
    fun `an empty column reads back as an empty list instead of one blank genre`() {
        assertEquals(emptyList<String>(), converter.toList(""))
        assertEquals(emptyList<String>(), converter.toList("   "))
    }

    @Test
    fun `an empty list is stored as an empty string`() {
        assertEquals("", converter.fromList(emptyList()))
    }
}
