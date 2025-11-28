package com.fictivestudios.demo.ui.fragments.main.registerPhoneNo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.LoginUserResponse
import com.fictivestudios.demo.data.responses.RegisterPhoneNoResponse
import com.fictivestudios.demo.data.responses.VerifyNumberRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterPhoneNoViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _registerPhoneNoResponse: MutableLiveData<Resource<BaseNetworkResponse<RegisterPhoneNoResponse>>?> =
        MutableLiveData()
    val registerPhoneNoResponse: LiveData<Resource<BaseNetworkResponse<RegisterPhoneNoResponse>>?>
        get() = _registerPhoneNoResponse

    private val _deletePhoneNoResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val deletePhoneNoResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _deletePhoneNoResponse

    var userData: LoginUserResponse? = null

    init {
        viewModelScope.launch { userData = getLoginUserData() }
    }

    fun registerPhoneNoApi(data: VerifyNumberRequest) = viewModelScope.launch {
        _registerPhoneNoResponse.value = Resource.Loading
        _registerPhoneNoResponse.value = repository.registerPhoneNo(data)
        _registerPhoneNoResponse.value = null
    }

    fun deletePhoneNoApi() = viewModelScope.launch {
        _deletePhoneNoResponse.value = Resource.Loading
        _deletePhoneNoResponse.value = repository.deletePhoneNumber(userData?.phone ?: "")
        _deletePhoneNoResponse.value = null
    }
}