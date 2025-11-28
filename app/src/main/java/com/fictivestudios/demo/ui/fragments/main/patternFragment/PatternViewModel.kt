package com.fictivestudios.demo.ui.fragments.main.patternFragment

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
import javax.inject.Inject

@HiltViewModel
class PatternViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    private val _createPatternResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val createPatternResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _createPatternResponse

    private val _deletePatternResponse: MutableLiveData<Resource<BaseNetworkResponse<Any>>?> =
        MutableLiveData()
    val deletePatternResponse: LiveData<Resource<BaseNetworkResponse<Any>>?>
        get() = _deletePatternResponse

    var userData: LoginUserResponse? = null

    init {
        viewModelScope.launch { userData = getLoginUserData() }
    }

    fun createPattern(data: ArrayList<String>) = viewModelScope.launch {
        _createPatternResponse.value = Resource.Loading
        _createPatternResponse.value = repository.createPattern(data)
        _createPatternResponse.value = null
    }

    fun deletePinCode(data: ArrayList<String>) = viewModelScope.launch {
        _deletePatternResponse.value = Resource.Loading
        _deletePatternResponse.value = repository.deletePattern(data)
        _deletePatternResponse.value = null
    }


    suspend fun saveUserPinType(isPinVerified: Int) {
        repository.saveUserPinType(isPinVerified)
    }
}