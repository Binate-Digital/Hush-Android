package com.fictivestudios.demo.ui.fragments.main.callLog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.CallLogResponse
import com.fictivestudios.demo.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {


    private val _callLogResponse: MutableLiveData<Resource<BaseNetworkResponse<ArrayList<CallLogResponse>>>?> =
        MutableLiveData()
    val callLogResponse: LiveData<Resource<BaseNetworkResponse<ArrayList<CallLogResponse>>>?>
        get() = _callLogResponse

    var userData: LoginUserResponse? = null

    init {
        viewModelScope.launch {
            userData = getLoginUserData()
        }
    }

    fun getAllCallLogs() = viewModelScope.launch {
        _callLogResponse.value = Resource.Loading
        _callLogResponse.value = repository.getAllCallLogs(userData?.phone ?: "")
        _callLogResponse.value
    }
}