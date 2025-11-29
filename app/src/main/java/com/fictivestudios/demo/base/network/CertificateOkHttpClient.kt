package com.fictivestudios.demo.base.network

import android.content.Context
import android.util.Log
import com.fictivestudios.demo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.cert.X509Certificate
import javax.net.ssl.*

class CertificateOkHttpClient(private val context: Context) {

    fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .build()
                )
            }

        // Logging only in DEBUG
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor { message -> Log.d("OKHTTP", message) }
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
        }

        try {
            // ----------------------------
            //  BYPASS SSL FOR DEBUG ONLY
            // ----------------------------
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>, authType: String
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>, authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> =
                        arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            val trustManager = trustAllCerts[0] as X509TrustManager

            builder.sslSocketFactory(sslContext.socketFactory, trustManager)
            builder.hostnameVerifier { _, _ -> true }

        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return builder.build()
    }
}
