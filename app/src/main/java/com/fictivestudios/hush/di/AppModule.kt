package com.fictivestudios.hush.di

import android.content.Context
import com.fictivestudios.hush.data.networks.AuthApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.fictivestudios.hush.base.network.BaseApi
import com.fictivestudios.hush.base.network.CertificateOkHttpClient
import com.fictivestudios.hush.utils.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @DelicateCoroutinesApi
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val certificateOkHttpClient = CertificateOkHttpClient(context)
        return certificateOkHttpClient.createOkHttpClient()
    }
    @DelicateCoroutinesApi
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Replace with your base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @DelicateCoroutinesApi
    @Provides
    fun provideBaseApi(
        retrofit: Retrofit
    ): BaseApi {
        return retrofit.create(BaseApi::class.java)
    }

    @DelicateCoroutinesApi
    @Provides
    fun provideAuthApiService(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }



//    @Provides
//    @Singleton
//    fun provideSocketIO(@ApplicationContext context: Context, dataPreferences: DataPreference): SocketIO {
//        return SocketIO(context, dataPreferences)
//    }
}