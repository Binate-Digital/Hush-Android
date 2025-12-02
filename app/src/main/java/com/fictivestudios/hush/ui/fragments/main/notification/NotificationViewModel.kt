package com.fictivestudios.hush.ui.fragments.main.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.NotificationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {


    private val _notificationResponse: MutableLiveData<Resource<BaseNetworkResponse<ArrayList<NotificationResponse>>>?> =
        MutableLiveData()
    val notificationResponse: LiveData<Resource<BaseNetworkResponse<ArrayList<NotificationResponse>>>?>
        get() = _notificationResponse

    fun getAllNotification() = viewModelScope.launch {
        _notificationResponse.value = Resource.Loading
        _notificationResponse.value = repository.getAllNotification()
        _notificationResponse.value = null
    }
}