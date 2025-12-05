package com.fictivestudios.hush.ui.fragments.main.address

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.response.BaseNetworkResponse
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.AddressResponse
import com.fictivestudios.hush.data.responses.ContactListResponse
import com.fictivestudios.hush.data.responses.ContactResponse
import com.fictivestudios.hush.data.responses.Contacts
import com.fictivestudios.hush.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {
    var addressList = ArrayList<AddressResponse>()

    var contactArray = arrayListOf<Contacts>()
    var page = 1
    private var pageSize = 10
    private var isLoadingMoreItems: Boolean = false
    var searchQuery = ""


    private val _contactListResponse: MutableLiveData<Resource<BaseNetworkResponse<ContactListResponse>>?> =
        MutableLiveData()
    val contactListResponse: LiveData<Resource<BaseNetworkResponse<ContactListResponse>>?>
        get() = _contactListResponse

    private val _createContactResponse: MutableLiveData<Resource<BaseNetworkResponse<ContactResponse>>?> =
        MutableLiveData()
    val createContactResponse: LiveData<Resource<BaseNetworkResponse<ContactResponse>>?>
        get() = _createContactResponse

    init {
        viewModelScope.launch {
            for (i in 1..10) {
                addressList.add(AddressResponse("John", R.drawable.persons))
            }
        }

    }

    var userData: LoginUserResponse? = null
    var callToken: String? = null
    fun getUserData()= viewModelScope.launch {
        userData = getLoginUserData()
        callToken = getCallToken()
    }

    fun createContactApi(
        fullName: String,
        lastName: String,
        phoneNo: String,
        notes: String,
        contactImage: MultipartBody.Part? = null

    ) = viewModelScope.launch {
        val response = repository.createContact(
            fullName
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            lastName
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            phoneNo
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            notes
                .toRequestBody("text/plain".toMediaTypeOrNull()),
            contactImage

        )
        _createContactResponse.value = Resource.Loading
        _createContactResponse.value = response
        _createContactResponse.value = null
    }


    fun getAllContactListApi(offset: Int,query:String = searchQuery) = viewModelScope.launch {
        _contactListResponse.value = Resource.Loading

        if (searchQuery != query) {
            searchQuery = query
        }
Log.d("query",query)
Log.d("search",searchQuery)
        val response = repository.getContactList(offset, pageSize,query)

        if (offset == 0) {
            page = 1
        }

        if (isLoadingMoreItems) {
            isLoadingMoreItems = false
        }

        when (response) {

            is Resource.Success -> {
                response.value.data?.let {
                    if (it.contacts.isNotEmpty()) {
                        if (page == 1) {
                            contactArray = arrayListOf()
                            contactArray.addAll(it.contacts)
                        } else {
                            contactArray.addAll(it.contacts)
                        }

                        if (it.contacts.size < pageSize) {
                            this@AddressViewModel.page = 0
                        }
                        it.contacts.clear()
                        it.contacts.addAll(contactArray)
                        _contactListResponse.value = response
                        _contactListResponse.value = null
                    } else {
                        _contactListResponse.value = response
                    }
                }
            }

            is Resource.Failure -> {
                _contactListResponse.value = response
            }

            else -> {
                _contactListResponse.value = response
            }
        }
    }

    fun loadNextPage() {
        if (page != 0 && !isLoadingMoreItems) {
            isLoadingMoreItems = true
            page += 1
            getAllContactListApi(page)
        } else {
            Log.d("Loading", "Loading more items in progress/No more items to load...")
        }
    }

}