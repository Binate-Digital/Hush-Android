package com.fictivestudios.hush.ui.fragments.main.pinFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.CreatePinCodeRequest
import com.fictivestudios.hush.data.responses.EditPinCodeRequest
import com.fictivestudios.hush.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinCodeViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _pinCodeResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val pinCodeResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _pinCodeResponse

    var userData: LoginUserResponse? = null

    init {
        viewModelScope.launch { userData = getLoginUserData() }
    }

    fun createPinCode(data: CreatePinCodeRequest) = viewModelScope.launch {
        _pinCodeResponse.value = Resource.Loading
        _pinCodeResponse.value = repository.createPinCode(data)
        _pinCodeResponse.value = null
    }

    fun editPinCode(data: EditPinCodeRequest) = viewModelScope.launch {
        _pinCodeResponse.value = Resource.Loading
        _pinCodeResponse.value = repository.editPinCode(data)
        _pinCodeResponse.value = null
    }

    suspend fun saveUserPinType(isPinVerified: Int) {
        repository.saveUserPinType(isPinVerified)
    }
}