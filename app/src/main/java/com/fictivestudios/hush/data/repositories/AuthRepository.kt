package com.fictivestudios.hush.data.repositories

import com.fictivestudios.hush.base.preference.DataPreference
import com.fictivestudios.hush.base.repository.BaseRepository
import com.fictivestudios.hush.data.networks.AuthApi
import com.fictivestudios.hush.data.responses.BlockUser
import com.fictivestudios.hush.data.responses.CreatePinCodeRequest
import com.fictivestudios.hush.data.responses.EditPinCodeRequest
import com.fictivestudios.hush.data.responses.LockTypeRequest
import com.fictivestudios.hush.data.responses.LoginUserRequest
import com.fictivestudios.hush.data.responses.OTPRequest
import com.fictivestudios.hush.data.responses.PatternRequest
import com.fictivestudios.hush.data.responses.RecoverUserRequest
import com.fictivestudios.hush.data.responses.ReportUser
import com.fictivestudios.hush.data.responses.SocialLoginRequest
import com.fictivestudios.hush.data.responses.VerifyCodeRequest
import com.google.gson.JsonArray
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    preferences: DataPreference

) : BaseRepository(api, preferences) {


    suspend fun loginUserApi(data: LoginUserRequest) = safeApiCall { api.login(data) }
    suspend fun deleteChatApi(chatId:String) = safeApiCall { api.deleteChat(preferences.accessToken.first(),chatId) }
    suspend fun recoverUserAccountApi(data: RecoverUserRequest) =
        safeApiCall { api.recoverAccount(data) }

    suspend fun logoutUserApi() = safeApiCall { api.logout(preferences.accessToken.first()) }

    suspend fun loginUserWithGoogleApi(data: SocialLoginRequest) =
        safeApiCall { api.socialLogin(data) }

    suspend fun verifyOtpApi(data: OTPRequest) = safeApiCall { api.verifyOtp(data) }
    suspend fun resendOtpApi(id: String) = safeApiCall { api.resendOtp(id) }
    suspend fun getContent(data: String) = safeApiCall { api.getPrivacyContent(data) }

    suspend fun getAllCallLogs(phoneNo: String) =
        safeApiCall { api.getAllCallLogs(preferences.accessToken.first(), phoneNo) }


    suspend fun getUserByToken() = safeApiCall {
        api.getUserByToken(preferences.accessToken.first())
    }

    suspend fun deleteUserByToken() = safeApiCall {
        api.deleteUserByToken(preferences.accessToken.first())
    }

    suspend fun deletePhoneNumber(phoneNo: String) = safeApiCall {
        api.deletePhoneNumber(preferences.accessToken.first(), phoneNo)
    }

    suspend fun notificationToggle() = safeApiCall {
        api.notificationToggle(preferences.accessToken.first())
    }
    suspend fun  getAllNotification() = safeApiCall {
        api.getAllNotification(preferences.accessToken.first())
    }

    suspend fun lockTypeToggle(data: LockTypeRequest) = safeApiCall {
        api.lockToggle(preferences.accessToken.first(), data)
    }

    suspend fun registerPhoneNo() = safeApiCall {
        api.registerPhoneNo(preferences.accessToken.first())
    }

    suspend fun getAllScreenShot(page: Int) = safeApiCall {
        api.getAllScreenShot(preferences.accessToken.first(), page)
    }

    suspend fun getAllSecurityFeature(page: Int) = safeApiCall {
        api.getAllSecurityFeatureList(preferences.accessToken.first(), page)
    }

    suspend fun deleteContactUserById(contactId: String) = safeApiCall {
        api.deleteContactById(preferences.accessToken.first(), contactId)
    }

    suspend fun deleteScreenShotByIds(screenShotIds: ArrayList<String>) = safeApiCall {

        // Convert your list to a JSON array string
        val jsonArray = JsonArray()
        screenShotIds.forEach {
            jsonArray.add(it)
        }
        val screenshotIdArrayString = jsonArray.toString()

        api.deleteScreenshots(preferences.accessToken.first(), screenshotIdArrayString)
    }


    suspend fun createPattern(patternIds: ArrayList<String>) = safeApiCall {
        api.createPattern(preferences.accessToken.first(), PatternRequest(patternIds))
    }

    suspend fun verifyPattern(patternIds: ArrayList<String>) = safeApiCall {
        api.verifyPattern(preferences.accessToken.first(), PatternRequest(patternIds))
    }

    suspend fun deletePattern(patternIds: ArrayList<String>) = safeApiCall {
        val jsonArray = JsonArray()
        patternIds.forEach {
            jsonArray.add(it)
        }
        val patternIdsArrayString = jsonArray.toString()
        api.deletePattern(preferences.accessToken.first(), patternIdsArrayString)
    }

    suspend fun deleteAttachmentByIds(attachmentsIds: ArrayList<String>) = safeApiCall {

        // Convert your list to a JSON array string
        val jsonArray = JsonArray()
        attachmentsIds.forEach {
            jsonArray.add(it)
        }
        val screenshotIdArrayString = jsonArray.toString()
        api.deleteAttachmentsById(preferences.accessToken.first(), screenshotIdArrayString)
    }

    suspend fun repostUser(id: String, text: String) = safeApiCall {
        api.reportUser(preferences.accessToken.first(), ReportUser(id, text))
    }

    suspend fun createPinCode(data: CreatePinCodeRequest) = safeApiCall {
        api.createPinCode(preferences.accessToken.first(), data)
    }

    suspend fun verifyPinCode(data: VerifyCodeRequest) = safeApiCall {
        api.verifyPin(preferences.accessToken.first(), data)
    }

    suspend fun editPinCode(data: EditPinCodeRequest) = safeApiCall {
        api.editPinCode(preferences.accessToken.first(), data)
    }

    suspend fun blockUser(id: String) = safeApiCall {
        api.blockUser(preferences.accessToken.first(), BlockUser(id))
    }

    suspend fun blockUserList() = safeApiCall {
        api.blockedUserList(preferences.accessToken.first())
    }

    suspend fun getContactUserById(userId: String) = safeApiCall {
        api.getContactById(preferences.accessToken.first(), userId)
    }

    suspend fun createNewProfileApi(
        fullName: RequestBody?,
        phoneNo: RequestBody?,
        description: RequestBody?,
        lat: RequestBody?,
        long: RequestBody?,
        address: RequestBody?,
        profileImage: MultipartBody.Part?, token: String? = null
    ) = safeApiCall {
        api.createNewProfile(
            token ?: preferences.accessToken.first(),
            fullName,
            phoneNo,
            description, lat, long, address, profileImage
        )
    }

    suspend fun uploadScreenShot(
        screenShot: MultipartBody.Part?
    ) = safeApiCall {
        api.uploadScreenShot(
            preferences.accessToken.first(), screenShot
        )
    }

    suspend fun createContact(
        fullName: RequestBody?,
        lastName: RequestBody?,
        phoneNo: RequestBody?,
        notes: RequestBody?,
        contactImage: MultipartBody.Part?, token: String? = null
    ) = safeApiCall {
        api.createContact(
            token ?: preferences.accessToken.first(),
            fullName,
            lastName, phoneNo,
            notes, contactImage
        )
    }

    suspend fun addSecurityFeature(
        files: ArrayList<MultipartBody.Part?>?
    ) = safeApiCall {
        api.addSecurityFeature(
            preferences.accessToken.first(),
            files
        )
    }

    suspend fun getContactList(page: Int, limit: Int, name: String = "") = safeApiCall {
        api.getContactList(preferences.accessToken.first(), page, limit, name)
    }

    suspend fun getCallTokenApi() = safeApiCall {
        api.getCallToken(preferences.accessToken.first())
    }

}