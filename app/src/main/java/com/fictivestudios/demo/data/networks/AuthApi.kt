package com.fictivestudios.demo.data.networks

import com.fictivestudios.demo.base.network.BaseApi
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.data.responses.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthApi : BaseApi {

    @POST("api/signin")
    suspend fun login(@Body data: LoginUserRequest): BaseNetworkResponse<SignUpUserResponse>

    @POST("api/recoverAccount")
    suspend fun recoverAccount(@Body data: RecoverUserRequest): BaseNetworkResponse<LoginUserResponse>

    @POST("api/verifyaccount")
    suspend fun verifyOtp(@Body data: OTPRequest): BaseNetworkResponse<LoginUserResponse>

    @POST("api/sociallogin")
    suspend fun socialLogin(@Body data: SocialLoginRequest): BaseNetworkResponse<LoginUserResponse>

    @POST("api/resendOTP")
    suspend fun resendOtp(@Query("id") id: String): BaseNetworkResponse<Any>

    @GET("api/getTcPp")
    suspend fun getPrivacyContent(@Query("type") type: String): BaseNetworkResponse<ContentData>

    @GET("user/callLog")
    suspend fun getAllCallLogs(
        @Header("Authorization") authToken: String,
        @Query("phoneNumber") phoneNumber: String
    ): BaseNetworkResponse<ArrayList<CallLogResponse>>

    @POST("user/blockContact")
    suspend fun blockUser(
        @Header("Authorization") authToken: String,
        @Body data: BlockUser
    ): BaseNetworkResponse<Any>

    @POST("user/ReportContact")
    suspend fun reportUser(
        @Header("Authorization") authToken: String,
        @Body data: ReportUser
    ): BaseNetworkResponse<Any>

    @POST("user/createPincode")
    suspend fun createPinCode(
        @Header("Authorization") authToken: String,
        @Body data: CreatePinCodeRequest
    ): BaseNetworkResponse<Any>

    @POST("user/verifyPin")
    suspend fun verifyPin(
        @Header("Authorization") authToken: String,
        @Body data: VerifyCodeRequest
    ): BaseNetworkResponse<Any>


    @POST("user/createPincode")
    suspend fun editPinCode(
        @Header("Authorization") authToken: String,
        @Body data: EditPinCodeRequest
    ): BaseNetworkResponse<Any>

    @GET("user/blockedContactList")
    suspend fun blockedUserList(
        @Header("Authorization") authToken: String
    ): BaseNetworkResponse<ArrayList<UserBlockList>>

    @POST("api/myProfile")
    suspend fun getUserByToken(@Header("Authorization") authToken: String): BaseNetworkResponse<LoginUserResponse>

    @DELETE("api/deleteAccount")
    suspend fun deleteUserByToken(@Header("Authorization") authToken: String): BaseNetworkResponse<LoginUserResponse>

    @DELETE("user/deletePhoneNumber")
    suspend fun deletePhoneNumber(
        @Header("Authorization") authToken: String,
        @Query("phoneNumber") phoneNumber: String
    ): BaseNetworkResponse<Any>

    @POST("api/notificationToggle")
    suspend fun notificationToggle(@Header("Authorization") authToken: String): BaseNetworkResponse<LoginUserResponse>

    @GET("api/notificationList")
    suspend fun getAllNotification(@Header("Authorization") authToken: String): BaseNetworkResponse<ArrayList<NotificationResponse>>

    @POST("user/lockToggle")
    suspend fun lockToggle(
        @Header("Authorization") authToken: String,
        @Body data: LockTypeRequest
    ): BaseNetworkResponse<Any>

    @POST("/user/createPattern")
    suspend fun createPattern(
        @Header("Authorization") authToken: String,
        @Body data: PatternRequest
    ): BaseNetworkResponse<Any>

    @POST("/user/verifyPattern")
    suspend fun verifyPattern(
        @Header("Authorization") authToken: String,
        @Body data: PatternRequest
    ): BaseNetworkResponse<Any>


    @DELETE("user/deletePattern")
    suspend fun deletePattern(
        @Header("Authorization") authToken: String,
        @Query("pattern") patternIds: String
    ): BaseNetworkResponse<Any>


    @POST("user/verifyNumber")
    suspend fun registerPhoneNo(
        @Header("Authorization") authToken: String,
        @Body data: VerifyNumberRequest
    ): BaseNetworkResponse<RegisterPhoneNoResponse>

    @GET("user/screenshotGallery")
    suspend fun getAllScreenShot(
        @Header("Authorization") authToken: String,
        @Query("offset") page: Int,
        @Query("limit") limit: Int = 10
    ): BaseNetworkResponse<ScreenshotsResponse>

    @GET("user/securityFeatureList")
    suspend fun getAllSecurityFeatureList(
        @Header("Authorization") authToken: String,
        @Query("offset") page: Int,
        @Query("limit") limit: Int = 10
    ): BaseNetworkResponse<SecurityFeatureResponse>

    @Multipart
    @POST("api/completeProfile")
    suspend fun createNewProfile(
        @Header("Authorization") authToken: String,
        @Part("name") fullName: RequestBody?,
        @Part("phone") phoneNo: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("lat") lat: RequestBody?,
        @Part("long") long: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part profileImage: MultipartBody.Part?,
    ): BaseNetworkResponse<LoginUserResponse>

    @Multipart
    @POST("/user/createScreenshot")
    suspend fun uploadScreenShot(
        @Header("Authorization") authToken: String,
        @Part screenShot: MultipartBody.Part?,
    ): BaseNetworkResponse<Any>

    @Multipart
    @POST("user/createContact")
    suspend fun createContact(
        @Header("Authorization") authToken: String,
        @Part("fname") fullName: RequestBody?,
        @Part("lname") lastName: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("notes") notes: RequestBody?,
        @Part contactImage: MultipartBody.Part?,
    ): BaseNetworkResponse<ContactResponse>

    @Multipart
    @POST("user/addAttachment")
    suspend fun addSecurityFeature(
        @Header("Authorization") authToken: String,
        @Part files: ArrayList<MultipartBody.Part?>?,
    ): BaseNetworkResponse<Attachments>

    @GET("user/contactList")
    suspend fun getContactList(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("name") name: String = ""
    ): BaseNetworkResponse<ContactListResponse>

    @GET("user/accessToken")
    suspend fun getCallToken(@Header("Authorization") authToken: String): BaseNetworkResponse<CallTokenResponse>

    @GET("user/contactDetails")
    suspend fun getContactById(
        @Header("Authorization") authToken: String,
        @Query("contactId") userId: String,
    ): BaseNetworkResponse<ContactResponse>

    @DELETE("user/deleteContact")
    suspend fun deleteContactById(
        @Header("Authorization") authToken: String,
        @Query("contactId") userId: String,
    ): BaseNetworkResponse<Any>

    @DELETE("user/deleteScreenshots")
    suspend fun deleteScreenshots(
        @Header("Authorization") authToken: String,
        @Query("screenshotId") screenshotId: String,
    ): BaseNetworkResponse<Any>

    @DELETE("user/deleteAttachments")
    suspend fun deleteAttachmentsById(
        @Header("Authorization") authToken: String,
        @Query("attachmentId") screenshotId: String,
    ): BaseNetworkResponse<Any>

}