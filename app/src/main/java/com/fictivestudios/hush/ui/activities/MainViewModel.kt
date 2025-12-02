package com.fictivestudios.hush.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.CallTokenResponse
import com.fictivestudios.hush.data.responses.LoginUserResponse
import com.fictivestudios.hush.data.responses.VerifyCodeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _uploadScreenShotResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val uploadScreenShotResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _uploadScreenShotResponse

    private val _verifyPinCodeResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val verifyPinCodeResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _verifyPinCodeResponse

    private val _patternResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val patternResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _patternResponse

    private val _callTokenResponse: MutableLiveData<Resource<BaseNetworkResponse<CallTokenResponse>>?> =
        MutableLiveData()
    val callTokenResponse: LiveData<Resource<BaseNetworkResponse<CallTokenResponse>>?>
        get() = _callTokenResponse

    var userData: LoginUserResponse? = null




    fun getUserData(
        isDataNull: (Boolean) -> Unit
    )= viewModelScope.launch {
        userData = getLoginUserData()
        if( getLoginUserData() == null){
            isDataNull(true)
        }else{
            isDataNull(false)
        }
    }

    fun uploadScreenShot(
        profileImage: MultipartBody.Part? = null,

        ) = viewModelScope.launch {
        val response = repository.uploadScreenShot(
            profileImage

        )
        _uploadScreenShotResponse.value = Resource.Loading
        _uploadScreenShotResponse.value = response
        _uploadScreenShotResponse.value = null
    }

    fun verifyPattern(data: ArrayList<String>) = viewModelScope.launch {
        _patternResponse.value = Resource.Loading
        _patternResponse.value = repository.verifyPattern(data)
        _patternResponse.value = null
    }

    fun verifyPinCode(
        data: VerifyCodeRequest

    ) = viewModelScope.launch {
        _verifyPinCodeResponse.value = Resource.Loading
        _verifyPinCodeResponse.value = repository.verifyPinCode(data)
        _verifyPinCodeResponse.value = null
    }

    fun getCallTokenApi() = viewModelScope.launch {
        _callTokenResponse.value = Resource.Loading
        _callTokenResponse.value = repository.getCallTokenApi()
        _callTokenResponse.value = null
    }

}