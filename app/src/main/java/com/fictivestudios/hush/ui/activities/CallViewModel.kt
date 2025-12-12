package com.fictivestudios.hush.ui.activities

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(val repository: AuthRepository) :
    BaseViewModel(repository) {

    var userData: LoginUserResponse? = null
    var callAccessToken = ""


    fun init(onGetData:(String?,String?)->Unit) =  viewModelScope.launch {
        userData = getLoginUserData()
        callAccessToken = getCallToken()
        onGetData(getLoginUserData()?.purchasedTwilioNumber,getCallToken())
        Log.d("CallViewModel",callAccessToken)
        Log.d("CallViewModel userData",userData.toString())

    }

}