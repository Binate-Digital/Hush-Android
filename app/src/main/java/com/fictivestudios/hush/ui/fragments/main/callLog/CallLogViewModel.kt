package com.fictivestudios.hush.ui.fragments.main.callLog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.CallLogResponse
import com.fictivestudios.hush.data.responses.LoginUserResponse
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


    fun init(data:(LoginUserResponse?)->Unit) {
        viewModelScope.launch {
           data(getLoginUserData())
        }
    }

    fun getAllCallLogs(phone:String) = viewModelScope.launch {
        _callLogResponse.value = Resource.Loading
        _callLogResponse.value = repository.getAllCallLogs(phone)
        _callLogResponse.value
    }
}