package com.fictivestudios.demo.base.network

import com.fictivestudios.demo.base.response.BaseNetworkResponse
import retrofit2.http.Header
import retrofit2.http.POST


interface BaseApi {
    @POST("api/signout")
    suspend fun logout(
        @Header("Authorization") authToken: String): BaseNetworkResponse<Any>
}