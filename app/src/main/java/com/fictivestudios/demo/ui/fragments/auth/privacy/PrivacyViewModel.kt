package com.fictivestudios.demo.ui.fragments.auth.privacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.ContentData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _privacyResponse: MutableLiveData<Resource<BaseNetworkResponse<ContentData>>?> =
        MutableLiveData()
    val privacyResponse: LiveData<Resource<BaseNetworkResponse<ContentData>>?>
        get() = _privacyResponse

    fun getPrivacyContent(data: String) = viewModelScope.launch {
        _privacyResponse.value = Resource.Loading
        _privacyResponse.value = repository.getContent(data)

    }

}