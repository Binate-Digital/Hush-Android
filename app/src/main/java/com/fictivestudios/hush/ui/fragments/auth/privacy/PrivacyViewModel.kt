package com.fictivestudios.hush.ui.fragments.auth.privacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.ContentData
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