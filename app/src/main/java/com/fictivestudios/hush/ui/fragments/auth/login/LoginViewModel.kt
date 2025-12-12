package com.fictivestudios.hush.ui.fragments.auth.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.LoginUserRequest
import com.fictivestudios.hush.data.responses.LoginUserResponse
import com.fictivestudios.hush.data.responses.RecoverUserRequest
import com.fictivestudios.hush.data.responses.SignUpUserResponse
import com.fictivestudios.hush.data.responses.SocialLoginRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    var deviceToken = ""
    var fcmToken:String? = ""

    fun init() {
        viewModelScope.launch {
            deviceToken = repository.getDeviceToken()
            getFirebaseTokenFromFireBase()
            Log.d("FCM TOKEN FROM LOGIN","$fcmToken")

        }
    }

    private val _loginUserResponse: MutableLiveData<Resource<BaseNetworkResponse<SignUpUserResponse>>?> =
        MutableLiveData()
    val loginUserResponse: LiveData<Resource<BaseNetworkResponse<SignUpUserResponse>>?>
        get() = _loginUserResponse

    private val _socialLoginUserResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val socialLoginUserResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _socialLoginUserResponse

    private val _recoverUserResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val recoverUserResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _recoverUserResponse

    fun loginUserApi(data: LoginUserRequest) = viewModelScope.launch {
        _loginUserResponse.value = Resource.Loading
        _loginUserResponse.value = repository.loginUserApi(data)
        _loginUserResponse.value = null
    }

    fun recoverUserAccountApi(data:RecoverUserRequest ) = viewModelScope.launch {
        _recoverUserResponse.value = Resource.Loading
        _recoverUserResponse.value = repository.recoverUserAccountApi(data)
        _recoverUserResponse.value = null
    }

    fun socialLoginUserApi(data: SocialLoginRequest) = viewModelScope.launch {
        _socialLoginUserResponse.value = Resource.Loading
        _socialLoginUserResponse.value = repository.loginUserWithGoogleApi(data)
        _socialLoginUserResponse.value = null

    }

    fun saveAccessToken(token: String?) = viewModelScope.launch {
        repository.saveAccessToken(token ?: "")
    }

   suspend fun saveLoggedInUser(userInfo: LoginUserResponse, token: String?)  {
        repository.saveAccessToken(token ?: "")
        repository.saveLoginUserId(userInfo._id?:"")
        repository.saveLoginUserInfo(userInfo)
        repository.setUserLoggedIn()
    }
    private fun getFirebaseTokenFromFireBase() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(
                    "firebase_token_failed",
                    "Fetching FCM registration token failed",
                    task.exception
                )
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.d("FCM TOKEN", token!!)
            fcmToken = token

        })
    }
}