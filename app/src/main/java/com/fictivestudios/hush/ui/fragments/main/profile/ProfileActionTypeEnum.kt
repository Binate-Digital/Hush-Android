package com.fictivestudios.hush.ui.fragments.main.profile

enum class ProfileActionTypeEnum(private val type: String) {

    NONE("None"),
    BLOCK("signup_fragment"),
    REPORT("login_fragment"),
    DELETE("camera_fragment");

    companion object {
        fun fromInt(value: String) = ProfileActionTypeEnum.values().first { it.type == value }
    }

    fun getValue(): String {
        return type
    }
}
