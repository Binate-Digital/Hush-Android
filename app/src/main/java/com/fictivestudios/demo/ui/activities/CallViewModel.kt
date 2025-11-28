package com.fictivestudios.demo.ui.activities

import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(val repository: AuthRepository) :
    BaseViewModel(repository) {

    var userData: LoginUserResponse? = null
    var callAccessToken = ""

    init {
        viewModelScope.launch {
            userData = getLoginUserData()
            callAccessToken = getCallToken()
        }
    }
}