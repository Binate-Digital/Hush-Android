package com.fictivestudios.hush.ui.fragments.auth.login


enum class LoginVerifiedEnum(private val type: Int) {

    USER_NOT_VERIFIED(0),
    USER_VERIFIED(1);


    companion object {
        fun fromInt(value: Int) = LoginVerifiedEnum.values().first { it.type == value }
    }

    fun getValue(): Int {
        return type
    }
}

enum class LoginUserDeleteEnum(private val type: Int) {

    USER_NOT_DELETED(0),
    USER_DELETED(1);


    companion object {
        fun fromInt(value: Int) = LoginUserDeleteEnum.values().first { it.type == value }
    }

    fun getValue(): Int {
        return type
    }
}


enum class LoginProfileVerifiedEnum(private val type: Int) {

    USER_PROFILE_NOT_COMPLETE(0),
    USER_PROFILE_COMPLETE(1);


    companion object {
        fun fromInt(value: Int) = LoginProfileVerifiedEnum.values().first { it.type == value }
    }

    fun getValue(): Int {
        return type
    }
}

