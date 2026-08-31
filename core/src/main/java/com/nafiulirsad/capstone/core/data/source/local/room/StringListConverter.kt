package com.nafiulirsad.capstone.core.data.source.local.room

import androidx.room.TypeConverter

/** Room has no native list column, so the genre list is stored as one delimited string. */
class StringListConverter {

    @TypeConverter
    fun fromList(values: List<String>): String = values.joinToString(SEPARATOR)

    @TypeConverter
    fun toList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(SEPARATOR)

    private companion object {
        const val SEPARATOR = "|"
    }
}
