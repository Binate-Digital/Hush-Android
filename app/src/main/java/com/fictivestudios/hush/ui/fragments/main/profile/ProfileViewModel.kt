package com.fictivestudios.hush.ui.fragments.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.ContactResponse
import com.fictivestudios.hush.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class
ProfileViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    var listener: ApiResponse? = null


    private val _loginUserResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val loginUserResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _loginUserResponse

    private val _blockUserResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val blockUserResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _blockUserResponse

    private val _deleteUserResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val deleteUserResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _deleteUserResponse

    private val _reportUserResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val reportUserResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _reportUserResponse

    private val _createNewProfileResponse: MutableLiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?> =
        MutableLiveData()
    val createNewProfileResponse: LiveData<Resource<BaseNetworkResponse<LoginUserResponse>>?>
        get() = _createNewProfileResponse

    private val _contactUserProfileResponse: MutableLiveData<Resource<BaseNetworkResponse<ContactResponse>>?> =
        MutableLiveData()
    val contactUserProfileResponse: LiveData<Resource<BaseNetworkResponse<ContactResponse>>?>
        get() = _contactUserProfileResponse


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


    suspend fun saveLoggedInUser(userInfo: LoginUserResponse) {
        repository.saveLoginUserId(userInfo._id?:"")
        repository.saveUserProfileData(userInfo)
        repository.setUserLoggedIn()
    }

    fun reportUser(id: String, text: String) = viewModelScope.launch {
        _reportUserResponse.value = Resource.Loading
        _reportUserResponse.value = repository.repostUser(id, text)
        _reportUserResponse.value = null
    }

    fun blockUser(id: String) = viewModelScope.launch {
        _blockUserResponse.value = Resource.Loading
        _blockUserResponse.value = repository.blockUser(id)
        _blockUserResponse.value = null
    }
    fun deleteContactById(id: String) = viewModelScope.launch {
        _deleteUserResponse.value = Resource.Loading
        _deleteUserResponse.value = repository.deleteContactUserById(id)
        _deleteUserResponse.value = null
    }

    fun getUserByToken() = viewModelScope.launch {
        _loginUserResponse.value = Resource.Loading
        _loginUserResponse.value = repository.getUserByToken()
    }

    fun getContactUserProfile(userId: String) = viewModelScope.launch {
        _contactUserProfileResponse.value = Resource.Loading
        _contactUserProfileResponse.value = repository.getContactUserById(userId)
    }
}

fun interface ApiResponse {
    fun response(isApiSuccess: Boolean,userActionType: String)
}