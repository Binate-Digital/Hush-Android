package com.fictivestudios.hush.ui.fragments.auth

enum class FragmentRouteEnumType(private val type: String) {

    NONE("None"),
    SIGNUP("signup_fragment"),
    LOGIN("login_fragment"),
    CAMERA("camera_fragment"),
    HOME("home_fragment"),
    ALBUM("album_fragment"),
    FORGOT_PASSWORD("forgot_fragment");

    companion object {
        fun fromInt(value: String) = FragmentRouteEnumType.values().first { it.type == value }
    }

    fun getValue(): String {
        return type
    }
}
