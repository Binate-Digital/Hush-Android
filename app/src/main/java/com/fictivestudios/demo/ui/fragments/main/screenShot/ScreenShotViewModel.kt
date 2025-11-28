package com.fictivestudios.demo.ui.fragments.main.screenShot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.Screenshots
import com.fictivestudios.demo.data.responses.ScreenshotsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreenShotViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {


    var screenShotArray = arrayListOf<Screenshots>()

    var page = 1
    private var pageSize = 10
    private var isLoadingMoreItems: Boolean = false


    private val _screenShotResponse: MutableLiveData<Resource<BaseNetworkResponse<ScreenshotsResponse>>?> =
        MutableLiveData()
    val screenShotResponse: LiveData<Resource<BaseNetworkResponse<ScreenshotsResponse>>?>
        get() = _screenShotResponse

    private val _deleteScreenShotResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val deleteScreenShotResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _deleteScreenShotResponse


    fun getAllScreenShotApi(offset: Int) = viewModelScope.launch {
        _screenShotResponse.value = Resource.Loading

        val response = repository.getAllScreenShot(offset)

        if (offset == 1) {
            page = 1
        }

        if (isLoadingMoreItems) {
            isLoadingMoreItems = false
        }

        when (response) {

            is Resource.Success -> {
                response.value.data?.screenshots?.let {
                    if (page == 1) {
                        screenShotArray = arrayListOf()
                        screenShotArray.addAll(it)
                    } else {
                        screenShotArray.addAll(it)
                    }

                    if (it.size < pageSize) {
                        this@ScreenShotViewModel.page = 0
                    }
                    it.clear()
                    it.addAll(screenShotArray)

                }
                _screenShotResponse.value = response
                _screenShotResponse.value = null
            }

            is Resource.Failure -> {
                _screenShotResponse.value = response
            }

            else -> {
                _screenShotResponse.value = response
            }
        }
    }

    fun deleteScreenShotByIds(screenShotIds: ArrayList<String>) = viewModelScope.launch {
        _deleteScreenShotResponse.value = Resource.Loading
        _deleteScreenShotResponse.value = repository.deleteScreenShotByIds(screenShotIds)
        _deleteScreenShotResponse.value = null
    }



    fun loadNextPage() {
        if (page != 0 && !isLoadingMoreItems) {
            isLoadingMoreItems = true
            page++
            getAllScreenShotApi(page)
        } else {
            Log.d("Loading", "Loading more items in progress/No more items to load...")
        }
    }
}