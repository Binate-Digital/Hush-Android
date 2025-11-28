package com.fictivestudios.demo.base.response

data class BaseNetworkResponse<T>(
    val status: Int,
    val message: String,
    val token: String? = null,
    val data: T?
)