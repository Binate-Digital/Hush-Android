package com.fictivestudios.hush.utils

import java.lang.Enum
import kotlin.String
import kotlin.let

enum class TimeFormatEnum {
    MILLIS, SECONDS, MINUTES, HOUR, DAY;

    fun canonicalForm(): String {
        return name
    }

    companion object {
        fun fromCanonicalForm(canonical: String?): TimeFormatEnum? {
            return canonical?.let { Enum.valueOf(TimeFormatEnum::class.java, it) }
        }
    }
}