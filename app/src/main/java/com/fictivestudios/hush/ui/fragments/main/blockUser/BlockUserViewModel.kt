package com.fictivestudios.hush.ui.fragments.main.blockUser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.UserBlockList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockUserViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {


    private val _blockUserListResponse: MutableLiveData<Resource<BaseNetworkResponse<ArrayList<UserBlockList>>>?> =
        MutableLiveData()
    val blockUserListResponse: LiveData<Resource<BaseNetworkResponse<ArrayList<UserBlockList>>>?>
        get() = _blockUserListResponse

    private val _blockUserResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val blockUserResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _blockUserResponse

    fun getBlockUserList() = viewModelScope.launch {
        _blockUserListResponse.value = Resource.Loading
        _blockUserListResponse.value = repository.blockUserList()
    }

    fun blockUser(id: String) = viewModelScope.launch {
        _blockUserResponse.value = Resource.Loading
        _blockUserResponse.value = repository.blockUser(id)
        _blockUserResponse.value = null
    }

}