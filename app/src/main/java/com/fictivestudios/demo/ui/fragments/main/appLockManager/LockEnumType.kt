package com.fictivestudios.demo.ui.fragments.main.appLockManager

enum class LockEnumType(private val type: String) {

    NONE("none"),
    PIN("pin"),
    FINGER_PRINT("fingerprint"),
    IS_FOR_PIN("Pin"),
    IS_FOR_PATTERN("Pattern"),
    IS_FOR_FINGER_PRINT("Fingerprint"),
    PATTERN("pattern");

    companion object {
        fun fromString(value: String) = LockEnumType.values().first { it.type == value }
    }

    fun getValue(): String {
        return type
    }
}
