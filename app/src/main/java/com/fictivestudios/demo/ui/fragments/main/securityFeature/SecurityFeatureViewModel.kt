package com.fictivestudios.demo.ui.fragments.main.securityFeature

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.Attachments
import com.fictivestudios.demo.data.responses.SecurityFeatureResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class SecurityFeatureViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {


    var securityFeatureList = arrayListOf<Attachments>()

    var page = 1
    private var pageSize = 10
    private var isLoadingMoreItems: Boolean = false


     val _securityFeatureResponse: MutableLiveData<Resource<BaseNetworkResponse<SecurityFeatureResponse>>?> =
        MutableLiveData()
    val securityFeatureResponse: LiveData<Resource<BaseNetworkResponse<SecurityFeatureResponse>>?>
        get() = _securityFeatureResponse

    private val _deleteSecurityFeatureResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val deleteSecurityFeatureResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _deleteSecurityFeatureResponse

    private val _addSecurityFeatureAttachmentsResponse: MutableLiveData<Resource<BaseNetworkResponse<Attachments>>?> =
        MutableLiveData()
    val addSecurityFeatureAttachmentsResponse: LiveData<Resource<BaseNetworkResponse<Attachments>>?>
        get() = _addSecurityFeatureAttachmentsResponse


    fun getAllSecurityFeatureApi(offset: Int) = viewModelScope.launch {
        _securityFeatureResponse.value = Resource.Loading
        val response = repository.getAllSecurityFeature(offset)

        if (offset == 1) {
            page = 1
        }

        if (isLoadingMoreItems) {
            isLoadingMoreItems = false
        }

        when (response) {
            is Resource.Success -> {
                response.value.data?.attachments?.let {
                        if (page == 1) {
                            securityFeatureList = arrayListOf()
                            securityFeatureList.addAll(it)
                        } else {
                            securityFeatureList.addAll(it)
                        }

                        if (it.size < pageSize) {
                            this@SecurityFeatureViewModel.page = 0
                        }
                        it.clear()
                        it.addAll(securityFeatureList)
                }
                _securityFeatureResponse.value = response
                _securityFeatureResponse.value = null
            }

            is Resource.Failure -> {
                _securityFeatureResponse.value = response
            }

            else -> {
                _securityFeatureResponse.value = response
            }
        }
    }

    fun deleteSecurityFeatureByIds(screenShotIds: ArrayList<String>) = viewModelScope.launch {
        _deleteSecurityFeatureResponse.value = Resource.Loading
        _deleteSecurityFeatureResponse.value = repository.deleteAttachmentByIds(screenShotIds)
        _deleteSecurityFeatureResponse.value = null
    }

    fun addAttachments(files:ArrayList<MultipartBody.Part?>?) = viewModelScope.launch{
        _addSecurityFeatureAttachmentsResponse.value = Resource.Loading
        _addSecurityFeatureAttachmentsResponse.value = repository.addSecurityFeature(files)
        _addSecurityFeatureAttachmentsResponse.value = null

    }

    fun loadNextPage() {
        if (page != 0 && !isLoadingMoreItems) {
            isLoadingMoreItems = true
            page++
            getAllSecurityFeatureApi(page)
        } else {
            Log.d("Loading", "Loading more items in progress/No more items to load...")
        }
    }


}