package com.fictivestudios.demo.ui.fragments.auth.createNewProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.response.BaseNetworkResponse
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class CreateNewProfileViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _createNewProfileResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val createNewProfileResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _createNewProfileResponse


    fun createNewProfileApi(
        name: String,
        phoneNo: String,
        description: String,
        lat: String,
        long: String,
        address: String,
        profileImage: MultipartBody.Part? = null,
        token: String

    ) = viewModelScope.launch {
        val response = repository.createNewProfileApi(
            name
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            phoneNo
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            description
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            lat
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            long
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            address
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            profileImage, token

        )
        _createNewProfileResponse.value = Resource.Loading
        _createNewProfileResponse.value = response
        _createNewProfileResponse.value = null
    }


    suspend fun saveLoggedInUser(userInfo: LoginUserResponse, token: String?)  {
        repository.saveAccessToken(token ?: "")
        repository.saveLoginUserId(userInfo._id)
        repository.saveUserProfileData(userInfo)
        repository.setUserLoggedIn()
    }

}