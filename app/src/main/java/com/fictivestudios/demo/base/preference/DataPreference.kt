package com.fictivestudios.demo.base.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fictivestudios.demo.base.preference.DataPreference.Companion.APPLICATION_ID
import com.fictivestudios.demo.data.responses.LoginUserResponse
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = APPLICATION_ID)

class DataPreference @Inject constructor(
    @ApplicationContext context: Context
) {

    // Make sure the context is not null before assigning it to appContext
    private val appContext =
        context.applicationContext ?: throw IllegalArgumentException("Context cannot be null")

    val accessToken: Flow<String>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN] ?: ""
        }

    val refreshToken: Flow<String>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN] ?: ""
        }


    val deviceToken: Flow<String>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[DEVICE_TOKEN] ?: ""
        }

    val callToken: Flow<String>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[CALL_TOKEN] ?: ""
        }

    val loginUserId: Flow<String>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[USER_ID] ?: ""
        }

    val isUserLogin: Flow<Boolean>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[IS_LOGIN] ?: false
        }

    val isDeviceModeDark: Flow<Boolean>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[DEVICE_MODE] ?: false
        }

    suspend fun saveAccessTokens(accessToken: String) {
        appContext.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = "Bearer $accessToken"
        }
    }


    suspend fun saveDeviceTokens(deviceToken: String) {
        appContext.dataStore.edit { preferences ->
            preferences[DEVICE_TOKEN] = deviceToken
        }
    }

    suspend fun saveCallTokens(deviceToken: String) {
        appContext.dataStore.edit { preferences ->
            preferences[CALL_TOKEN] = deviceToken
        }
    }

    suspend fun getBooleanData(key: Preferences.Key<Boolean>): Boolean =
        appContext.dataStore.data.map { preferences ->
            preferences[key] ?: false
        }.first()

    suspend fun setBooleanData(key: Preferences.Key<Boolean>, value: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun getStringData(key: Preferences.Key<String>): String =
        appContext.dataStore.data.map { preferences ->
            preferences[key] ?: ""
        }.first()

    suspend fun setStringData(key: Preferences.Key<String>, value: String) {
        appContext.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun updatePhoneNoRegisterData(phoneSid: String, phoneNo: String? = null) {
        val userData = Gson().fromJson(
            getStringData(USER_INFO),
            LoginUserResponse::class.java
        )
        userData.phoneSid = phoneSid
        if (phoneNo != null) {
            userData.phone = phoneNo
        }

        saveUserProfileData(userData)
    }
    suspend fun saveUserProfileData(userInfo: LoginUserResponse) {
        setStringData(USER_INFO, Gson().toJson(userInfo))
    }

    suspend fun getIntegerData(key: Preferences.Key<Int>): Int =
        appContext.dataStore.data.map { preferences ->
            preferences[key] ?: -1
        }.first()

    suspend fun setIntegerData(key: Preferences.Key<Int>, value: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun clear() {
        appContext.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun clearLoginDataForUpdate() {
        setStringData(USER_INFO, "")
    }

    suspend fun performLogout() {
        setBooleanData(IS_LOGIN, false)
        setBooleanData(DEVICE_MODE, false)
        setStringData(USER_ID, "")
        setStringData(USER_INFO, "")
        setStringData(ACCESS_TOKEN, "")
        setStringData(CALL_TOKEN, "")
        setStringData(USER_PHONE_SID, "")
        setStringData(REFRESH_TOKEN, "")
    }

    suspend fun setUpdateUserProfile(userInfo: LoginUserResponse) {
        setStringData(USER_INFO, Gson().toJson(userInfo))

    }

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("key_access_token")
        private val CALL_TOKEN = stringPreferencesKey("key_call_token")
        private val REFRESH_TOKEN = stringPreferencesKey("key_refresh_token")
        private val DEVICE_TOKEN = stringPreferencesKey("key_device_token")
         val DEVICE_MODE = booleanPreferencesKey("key_device_mode")
        val IS_LOGIN = booleanPreferencesKey("key_is_login")
        val USER_ID = stringPreferencesKey("key_user_id")
        val USER_INFO = stringPreferencesKey("key_user_info")
        val USER_PHONE_SID = stringPreferencesKey("key_user_phone_sid")
        val APPLICATION_ID = "com.example.intery"
    }
}