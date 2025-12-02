package com.fictivestudios.hush.ui.fragments.main.dialPad

import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.responses.LoginUserResponse
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