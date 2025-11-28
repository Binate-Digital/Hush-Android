package com.fictivestudios.demo.data.responses

import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize


data class SignUpUserResponse(
    val _id: String,
    var isDeleted: Int? = null
)

data class RecoverUserRequest(
    val id: String
)

data class UserBlockList(
    val contactId: String,
    val name: String,
    val contactImage: String,
)

data class CallLogResponse(

    val sid: String,
    val dateCreated: String,
    val dateUpdated: String,
    val parentCallSid: String,
    val accountSid: String,
    val to: String,
    val toFormatted: String,
    val from: String,
    val fromFormatted: String,
    val phoneNumberSid: String,
    val status: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val price: Double,
    val priceUnit: String,
    val direction: String,
    val answeredBy: String,
    val apiVersion: String,
    val forwardedFrom: String,
    val groupSid: String,
    val callerName: String,
    val queueTime: Int,
    val trunkSid: String,
    val uri: String,
    val subresourceUris: SubresourceUris
)


data class SubresourceUris(

    val user_defined_messages: String,
    val notifications: String,
    val recordings: String,
    val streams: String,
    val payments: String,
    val user_defined_message_subscriptions: String,
    val siprec: String,
    val events: String
)

data class ContentData(
    val url: String,
    val _id: String,
    val termCondition: String,
    val privacyPolicy: String,
    val aboutUs: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class LoginUserRequest(
    val email: String,
    val devicetoken: String,
    val devicetype: String,
)

data class CallTokenResponse(val token: String)

data class SocialLoginRequest(
    val socialToken: String,
    val socialType: String,
    val deviceToken: String,
    val deviceType: String,
    val name: String? = null,
    val email: String? = null,
    val socialPhone: String? = null,
)

data class AddressResponse(

    val userName: String,
    val userImage: Int
)

data class ChatListResponse(
    val userName: String,
    val lastMessage: String,
    val timeStamp: String,
    val userImage: Int,
)

data class Compress(
    val videoPath: String?,
    val fileName: String,
    val uri: Uri,
    val newFileSize: String,
    val progress: Float = 0F
)

data class Message(val id: Int, val userName: String, val userMessage: String, val userImage: Int)
data class NotificationResponse(

    val _id: String,
    val userId: String,
    val title: String,
    val body: String,
    val notificationType: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val created_at: String,
    val updated_at: String,
    val user: User
)

data class User(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val profile_image: String
)

data class OTPRequest(
    val otp: String,
    val id: String
)

@Parcelize
data class Service(
    val _id: String,
    val title: String,
    val content: String,
    val serviceImage: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val isBooked: Int
) : Parcelable


data class MapResponse(
    var name: String,
    var state: String,
    var city: String,
    var longitude: Double,
    var latitude: Double,
    var latlong: LatLng? = null
)


data class Location(

    val type: String,
    val address: String? = null,
    val coordinates: ArrayList<Double>
)

data class ReportUser(val contactId: String, val text: String)
data class BlockUser(val contactId: String)
data class VerifyCodeRequest(val pincode: String)
data class CreatePinCodeRequest(val newPincode: String, val confirmPincode: String)
data class EditPinCodeRequest(
    val existingPincode: String,
    val newPincode: String,
    val confirmPincode: String
)

data class LockTypeRequest(val lockType: String)
data class PatternRequest(val pattern: ArrayList<String>)
data class VerifyNumberRequest(val name: String, val phone: String)

data class RegisterPhoneNoResponse(

    val accountSid: String,
    val callSid: String,
    val friendlyName: String,
    val phoneNumber: String,
    val validationCode: String
)

data class RegisterPhoneNoData(

    val accountSid: String,
    val callSid: String,
    val friendlyName: String,
    val phoneNumber: String,
    val validationCode: String
)

data class LoginUserResponse(

    val location: Location,
    val _id: String,
    val email: String,
    var phone: String,
    val socialPhone: String,
    val name: String,
    val profileImage: String,
    val description: String,
    val otp: Int,
    val notification: Int,
    val stripeId: String,
    val block: Boolean,
    val isVerified: Int,
    var phoneSid: String,
    val isForget: Int,
    val isProfileCompleted: Int,
    val token: String,
    val searchCount: Int,
    val userSocialToken: String,
    val userSocialType: String,
    val userDeviceType: String,
    val userDeviceToken: String,
    val isDeleted: Int,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    var fingerprintLock: Int,
    var pinLock: Int,
    val pincode: Int,
    val faceLock: Int,
    val passtype: String,
    var patternLock: Int,
    var pinVerified: Int
)

data class ContactResponse(

    val userId: String,
    val fname: String,
    val lname: String,
    val fullName: String,
    val phone: String,
    val notes: String?,
    val contactImage: String?,
    val _id: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class ContactListResponse(
    val totalContact: Int,
    val contacts: ArrayList<Contacts>
)

data class Contacts(
    val _id: String,
    val fname: String,
    val lname: String,
    val contactImage: String
)


data class ScreenshotsResponse(
    val totalScreenshots: Int,
    val screenshots: ArrayList<Screenshots>
)

data class Screenshots(
    val _id: String? = null,
    val userId: String? = null,
    val image: String? = null,
    val time: String? = null,
    val date: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null,
    var isSelected: Boolean = false,
    var isMultipleSelectionEnabled: Boolean = false,
)

data class SecurityFeatureResponse(

    val totalAttachments: Int,
    val attachments: ArrayList<Attachments>
)

data class Attachments(

    val _id: String,
    val userId: String,
    val file: String?,
    val thumbnail: String?,
    val type: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    var isSelected: Boolean = false,
    var isMultipleSelectionEnabled: Boolean = false
)