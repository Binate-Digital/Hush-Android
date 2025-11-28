package com.fictivestudios.demo.base.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.repository.BaseRepository
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.data.responses.RegisterPhoneNoData
import kotlinx.coroutines.launch


abstract class BaseViewModel(private val repository: BaseRepository) : ViewModel() {


    suspend fun getLoginUserId() = repository.getLoginUserId()
    suspend fun getLoginUserData() = repository.getLoginUserData()


    private val _logoutUserResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>?>?> =
        MutableLiveData()
    val logoutUserResponse: LiveData<Resource<BaseNetworkResponse<Any>?>?>
        get() = _logoutUserResponse

    var isDarkModeSelected = false

    init {
        viewModelScope.launch {
            isDarkModeSelected = getDeviceThemeSelection()
        }

    }

    // suspend fun getUserInfoData() = repository.getLoginUserData()
//
//    suspend fun saveUserProfileData(data:LoginUserResponse) = repository.saveUserProfileData(data)

    fun deleteUser() = viewModelScope.launch {
        repository.userLogout()
    }

    fun performLogout() = viewModelScope.launch {
        _logoutUserResponse.value = Resource.Loading
        _logoutUserResponse.value = repository.logout()
        _logoutUserResponse.value = null
    }

    fun preferenceLogout() = viewModelScope.launch {
        repository.userLogout()
    }

    suspend fun saveCallToken(token: String) {
        repository.saveCallToken(token)
    }

    suspend fun saveDeviceTheme(value: Boolean)  {
        repository.setDeviceThemeSelection(value)
    }

    suspend fun getDeviceThemeSelection(): Boolean {
        return repository.getDeviceThemeSelection()
    }

    fun savePhoneInfo(phoneInfo: RegisterPhoneNoData) = viewModelScope.launch {
        repository.saveRegisterPhoneNoData(phoneInfo)
    }

    suspend fun getPhoneInfo(): RegisterPhoneNoData? = repository.getRegisterPhoneNoData()
    fun updatePhoneNoSidInUserData(phoneSid: String, phone: String? = null) =
        viewModelScope.launch {
            repository.updatePhoneNoRegisterData(phoneSid, phone)
        }

    suspend fun updatePatternLock(patternLock: Int) {
        repository.updatePatternLock(patternLock)
    }


    suspend fun getCallToken() = repository.getCallToken()

}