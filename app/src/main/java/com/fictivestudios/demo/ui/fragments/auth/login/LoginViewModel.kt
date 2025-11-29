package com.fictivestudios.demo.ui.fragments.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.LoginUserRequest
import com.fictivestudios.demo.data.responses.LoginUserResponse
import com.fictivestudios.demo.data.responses.RecoverUserRequest
import com.fictivestudios.demo.data.responses.SignUpUserResponse
import com.fictivestudios.demo.data.responses.SocialLoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    var deviceToken = ""

    init {
        viewModelScope.launch {
            deviceToken = repository.getDeviceToken()
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

}