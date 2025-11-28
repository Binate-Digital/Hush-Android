package com.fictivestudios.demo.ui.fragments.main.previewFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImagePreviewViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _deleteScreenShotResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val deleteScreenShotResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _deleteScreenShotResponse

    fun deleteScreenShotByIds(screenShotIds: ArrayList<String>) = viewModelScope.launch {
        _deleteScreenShotResponse.value = Resource.Loading
        _deleteScreenShotResponse.value = repository.deleteScreenShotByIds(screenShotIds)
        _deleteScreenShotResponse.value = null
    }

    fun deleteSecurityFeatureByIds(securityFeatureIds: ArrayList<String>) = viewModelScope.launch {
        _deleteScreenShotResponse.value = Resource.Loading
        _deleteScreenShotResponse.value = repository.deleteAttachmentByIds(securityFeatureIds)
        _deleteScreenShotResponse.value = null
    }
}