package com.fictivestudios.hush.base.network

import com.fictivestudios.hush.base.response.BaseNetworkResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface BaseApi {
    @POST("api/signout")
    suspend fun logout(
        @Header("Authorization") authToken: String): BaseNetworkResponse<Any>

    @POST("user/register-voip-token")
    suspend fun register(
        @Header("Authorization") authToken: String,
        @Body data:Register
    )
    : BaseNetworkResponse<Any>
}

data class Register (
        val voipToken:String,
        val type:String = "android",
        )