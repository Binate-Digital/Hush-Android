package com.fictivestudios.hush.ui.fragments.auth.privacy

enum class PrivacyTypeEnum(private val type: String) {
    PRIVACY("privacy_policy"),
    TERMS_AND_CONDITION("terms_and_conditions"),
    ABOUT_APP("information");

    companion object {
        fun fromString(value: String) = PrivacyTypeEnum.values().first { it.type == value }
    }

    fun getValue(): String {
        return type
    }
}
