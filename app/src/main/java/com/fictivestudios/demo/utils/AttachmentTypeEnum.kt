package com.fictivestudios.demo.utils

enum class AttachmentTypeEnum(val type:String) {
    NONE("none"), VIDEO("video"),IMAGE("image") ;

    companion object {
        fun fromInt(value: String) = AttachmentTypeEnum.values().first { it.type == value }
    }

    fun getValue(): String {
        return type
    }
}