package com.fictivestudios.hush.utils

enum class UserOptionEnum(val type: String) {
    NONE("none"),
    REPORT("report"),
    BLOCK("block"),
    DELETE("delete");

    companion object {
        fun fromInt(value: String) = UserOptionEnum.values().first { it.type == value }
    }

    fun getValue(): String {
        return type
    }
}