package com.fictivestudios.hush.ui.fragments.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _deleteUserResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val deleteUserResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _deleteUserResponse

    private val _notificationToggleResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val notificationToggleResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _notificationToggleResponse

    var userData: LoginUserResponse? = null

    init {
        viewModelScope.launch { userData = getLoginUserData() }
    }

    fun deleteUserByToken() = viewModelScope.launch {
        _deleteUserResponse.value = Resource.Loading
        _deleteUserResponse.value = repository.deleteUserByToken()
        _deleteUserResponse.value = null
    }

    suspend fun saveLoggedInUser(userInfo: LoginUserResponse)  {
        repository.saveUserProfileData(userInfo)
    }
    fun notificationToggle() = viewModelScope.launch {
        _notificationToggleResponse.value = Resource.Loading
        _notificationToggleResponse.value = repository.notificationToggle()
        _notificationToggleResponse.value = null
    }

}