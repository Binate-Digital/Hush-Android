package com.fictivestudios.demo.base.repository

import android.util.Log
import com.fictivestudios.demo.base.network.BaseApi
import com.fictivestudios.demo.base.network.SafeApiCall
import com.fictivestudios.demo.base.preference.DataPreference
import com.fictivestudios.demo.base.preference.DataPreference.Companion.USER_INFO
import com.fictivestudios.demo.base.preference.DataPreference.Companion.USER_PHONE_SID
import com.fictivestudios.demo.data.responses.LoginUserResponse
import com.fictivestudios.demo.data.responses.RegisterPhoneNoData
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import javax.inject.Singleton

@Singleton
abstract class BaseRepository(
    private val api: BaseApi,
    val preferences: DataPreference
) : SafeApiCall {


    suspend fun getLoginUserId(): String {
        return preferences.loginUserId.first()
    }


    suspend fun getLoginUserData(): LoginUserResponse? {
        Log.d("GetLogin Data","${Gson().fromJson(
            preferences.getStringData(USER_INFO),
            LoginUserResponse::class.java
        )}")
        return Gson().fromJson(
            preferences.getStringData(USER_INFO),
            LoginUserResponse::class.java
        )
    }


    suspend fun updatePhoneNoRegisterData(phoneSid: String, phoneNo: String? = null) {
        val userData = Gson().fromJson(
            preferences.getStringData(USER_INFO),
            LoginUserResponse::class.java
        )
        userData.phoneSid = phoneSid
        if (phoneNo != null) {
            userData.phone = phoneNo
        }

        saveUserProfileData(userData)
    }


    suspend fun updatePatternLock(patternLock: Int) {
        val userData = Gson().fromJson(
            preferences.getStringData(USER_INFO),
            LoginUserResponse::class.java
        )
        userData.patternLock = patternLock
        saveUserProfileData(userData)
    }


    suspend fun saveUserProfileData(userInfo: LoginUserResponse) {
        try {
            val json = Gson().toJson(userInfo)
            Log.d("saveUserProfileData", "Saving User: $json")
            preferences.setStringData(USER_INFO, json)
        } catch (e: Exception) {
            Log.e("saveUserProfileData", "Error saving user data", e)
        }
    }

    suspend fun saveRegisterPhoneNoData(phoneInfo: RegisterPhoneNoData) {
        preferences.setStringData(USER_PHONE_SID, Gson().toJson(phoneInfo))
    }

    suspend fun getRegisterPhoneNoData(): RegisterPhoneNoData? {
        return Gson().fromJson(
            preferences.getStringData(USER_PHONE_SID),
            RegisterPhoneNoData::class.java
        )
    }
    suspend fun saveUserPinType(isPinVerified: Int) {
        val user = Gson().fromJson(
            preferences.getStringData(USER_INFO),
            LoginUserResponse::class.java
        )
        user.pinVerified = isPinVerified
        preferences.setStringData(USER_INFO, Gson().toJson(user))
    }

    suspend fun saveUserPinOffOn(pinLock: Int) {
        val user = Gson().fromJson(
            preferences.getStringData(USER_INFO),
            LoginUserResponse::class.java
        )
        user.pinLock = pinLock
        user.fingerprintLock = 0
        user.patternLock = 0
        preferences.setStringData(USER_INFO, Gson().toJson(user))
    }

    suspend fun saveUserPatternOffOn(patternLock: Int) {
        val user = Gson().fromJson(
            preferences.getStringData(USER_INFO),
            LoginUserResponse::class.java
        )
        user.patternLock = patternLock
        Log.d("repoPattern",patternLock.toString())
        user.fingerprintLock = 0
        user.pinLock = 0
        preferences.setStringData(USER_INFO, Gson().toJson(user))
    }


    suspend fun saveUserFingerPrintOffOn(fingerPrintLock: Int) {
        val user = Gson().fromJson(
            preferences.getStringData(USER_INFO),
            LoginUserResponse::class.java
        )
        user.fingerprintLock = fingerPrintLock
        user.patternLock = 0
        user.pinLock = 0
        preferences.setStringData(USER_INFO, Gson().toJson(user))
    }


    suspend fun userLogout() {
        preferences.performLogout()
    }

    suspend fun logout() = safeApiCall {
        api.logout(preferences.accessToken.first())
    }


    suspend fun saveLoginUserId(userId: String) {
        preferences.setStringData(DataPreference.USER_ID, userId)
    }

    suspend fun saveLoginUserInfo(data: LoginUserResponse) {
        preferences.setStringData(USER_INFO, Gson().toJson(data))
    }


    suspend fun saveAccessToken(accessToken: String) {
        preferences.saveAccessTokens(accessToken)
    }

    suspend fun setUserLoggedIn() {
        preferences.setBooleanData(DataPreference.IS_LOGIN, true)
    }

    suspend fun clearUserPreferenceData() {
        preferences.performLogout()
    }

    suspend fun clearUserLoginData() {
        preferences.clearLoginDataForUpdate()
    }

    suspend fun getDeviceToken(): String {
        return preferences.deviceToken.first()
    }

    suspend fun setDeviceThemeSelection(value:Boolean) {
        preferences.setBooleanData(DataPreference.DEVICE_MODE, value)
    }
    suspend fun getDeviceThemeSelection(): Boolean {
        return preferences.isDeviceModeDark.first()
    }

    suspend fun saveCallToken(token: String) {
        preferences.saveCallTokens(token)
    }

    suspend fun getCallToken(): String {
        return preferences.callToken.first()
    }
}