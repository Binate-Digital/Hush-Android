package com.fictivestudios.demo.ui.fragments.auth.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.LoginUserResponse
import com.fictivestudios.demo.data.responses.OTPRequest
import com.fictivestudios.demo.data.responses.RecoverUserRequest
import com.fictivestudios.demo.data.responses.SocialLoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _otpVerificationResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val otpVerificationResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _otpVerificationResponse

    private val _resendOtpVerificationResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val resendOtpVerificationResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _resendOtpVerificationResponse

    private val _socialLoginUserResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val socialLoginUserResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _socialLoginUserResponse

    private val _recoverUserResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val recoverUserResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _recoverUserResponse

    fun verifyOtpApi(data: OTPRequest) = viewModelScope.launch {
        _otpVerificationResponse.value = Resource.Loading
        _otpVerificationResponse.value = repository.verifyOtpApi(data)
        _otpVerificationResponse.value = null
    }

    fun resendVerifyOtpApi(id: String) = viewModelScope.launch {
        _resendOtpVerificationResponse.value = Resource.Loading
        _resendOtpVerificationResponse.value = repository.resendOtpApi(id)
        _resendOtpVerificationResponse.value = null
    }

    fun saveAccessToken(token: String?) = viewModelScope.launch {
        repository.saveAccessToken(token ?: "")
    }

    fun recoverUserAccountApi(data: RecoverUserRequest) = viewModelScope.launch {
        _recoverUserResponse.value = Resource.Loading
        _recoverUserResponse.value = repository.recoverUserAccountApi(data)
        _recoverUserResponse.value = null
    }


    fun socialLoginUserApi(data: SocialLoginRequest) = viewModelScope.launch {
        _socialLoginUserResponse.value = Resource.Loading
        _socialLoginUserResponse.value = repository.loginUserWithGoogleApi(data)
        _socialLoginUserResponse.value = null

    }

    suspend fun saveLoggedInUser(userInfo: LoginUserResponse, token: String?) {
        repository.saveAccessToken(token ?: "")
        repository.saveLoginUserId(userInfo._id)
        repository.saveUserProfileData(userInfo)
        repository.setUserLoggedIn()
    }

}