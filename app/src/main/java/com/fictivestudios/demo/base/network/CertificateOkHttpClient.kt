package com.fictivestudios.demo.base.network

import android.content.Context
import android.util.Log
import com.fictivestudios.demo.BuildConfig
import com.fictivestudios.demo.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class CertificateOkHttpClient(private val context: Context) {

    fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().also {
                    it.addHeader("Accept", "application/json")
                }.build())
            }

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            builder.addInterceptor(logging)
        }

        try {
            // Load the custom certificate from resources
            val certificateInputStream: InputStream = context.resources.openRawResource(R.raw.server)
            val certificate = CertificateFactory.getInstance("X.509").generateCertificate(certificateInputStream)
            val certificateData = certificateInputStream.bufferedReader().use { it.readText() }
            Log.d("CertificateOkHttpClient", "Certificate Data: $certificateData")

            // Create a KeyStore containing our custom certificate
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("server", certificate)

            // Create a TrustManager that trusts the certificates in our KeyStore
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            val trustManagers = trustManagerFactory.trustManagers

            // Create the SSLContext with our custom TrustManager

            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustManagers, null)

            // Set the custom SSLContext to the OkHttpClient
            builder.sslSocketFactory(sslContext.socketFactory, trustManagers[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return builder.build()
    }
}
