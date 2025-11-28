package com.fictivestudios.demo.ui.fragments.main.appLockManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.LockTypeRequest
import com.fictivestudios.demo.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _lockTypeResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val lockTypeResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _lockTypeResponse

    var userData: LoginUserResponse? = null

    init {
        viewModelScope.launch { userData = getLoginUserData() }
    }

    suspend fun saveUserPinOffOn(pinLock: Int) {
        repository.saveUserPinOffOn(pinLock)
    }

    suspend fun saveUserPatternOffOn(patternLock: Int) {
        repository.saveUserPatternOffOn(patternLock)
    }

    suspend fun saveUserFingerPrintOffOn(fingerPrintLock: Int) {
        repository.saveUserFingerPrintOffOn(fingerPrintLock)
    }

    fun lockTypeToggle(data: LockTypeRequest) = viewModelScope.launch {
        _lockTypeResponse.value = Resource.Loading
        _lockTypeResponse.value = repository.lockTypeToggle(data)
        _lockTypeResponse.value = null
    }

}