package com.fictivestudios.hush.ui.fragments.main.registerPhoneNo

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
class RegisterPhoneNoViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _registerPhoneNoResponse: MutableLiveData<Resource<BaseNetworkResponse<Unit>>?> =
        MutableLiveData()
    val registerPhoneNoResponse: LiveData<Resource<BaseNetworkResponse<Unit>>?>
        get() = _registerPhoneNoResponse

    private val _deletePhoneNoResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val deletePhoneNoResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _deletePhoneNoResponse

    private val _userData: MutableLiveData<LoginUserResponse>? =
        MutableLiveData()
    val userData: LiveData<LoginUserResponse>?
        get() = _userData

    fun getUserData()= viewModelScope.launch {
        _userData?.value = getLoginUserData()
    }

    fun registerPhoneNoApi() = viewModelScope.launch {
        _registerPhoneNoResponse.value = Resource.Loading
        _registerPhoneNoResponse.value = repository.registerPhoneNo()
        _registerPhoneNoResponse.value = null
    }

}