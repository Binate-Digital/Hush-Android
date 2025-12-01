package com.fictivestudios.demo.ui.fragments.main.dialPad

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.LoginUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialPadViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

     var userData: LoginUserResponse? = null
     var callToken: String? = null
    fun getUserData()= viewModelScope.launch {
        userData = getLoginUserData()
        callToken = getCallToken()
    }
}