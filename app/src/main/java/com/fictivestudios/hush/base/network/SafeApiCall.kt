package com.fictivestudios.hush.base.network

import android.util.Log
import com.fictivestudios.hush.base.response.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

interface SafeApiCall {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {

                when (throwable) {
                    is HttpException -> {
                        val baseMessage = parseErrorMessage(throwable.response()?.errorBody())
                        Log.d("safeApiCall", "safeApiCall: ${baseMessage}")

                        if (throwable.code() == 401) {
                            // Resource.Success(apiCall.invoke())
                            Resource.Failure(
                                false,
                                throwable.code(),
                                throwable.response()?.errorBody(),
                                baseMessage.toString()
                            )
                        } else {
                            Resource.Failure(
                                false,
                                throwable.code(),
                                throwable.response()?.errorBody(),
                                baseMessage.toString()
                            )
                        }
                    }
                    is SocketTimeoutException->{
                        Resource.Failure(true, null, null, "Something went wrong.")
                    }

                    is IOException -> {
                        Resource.Failure(true, null, null, throwable.message.toString())
                    }

                    else -> {
                        Resource.Failure(true, null, null, throwable.message)
                    }
                }


            }
        }
    }
    private fun parseErrorMessage(errorBody: ResponseBody?): String? {
        return errorBody?.string()?.let { rawJson ->
            try {
                val jsonObject = JSONObject(rawJson)
                jsonObject.optString("message")
            } catch (e: Exception) {
                null
            }
        }
    }
}