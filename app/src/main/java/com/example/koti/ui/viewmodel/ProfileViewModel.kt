package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.GetCollectionUseCase
import com.example.koti.domain.SendResetPasswordUseCase
import com.example.koti.domain.SignOutUseCase
import com.example.koti.model.User
import com.example.koti.ui.util.Constants.SUCCESS
import com.example.koti.ui.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sendResetPasswordUseCase: SendResetPasswordUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCollectionUseCase: GetCollectionUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private val _resetPassword = MutableSharedFlow<Resource<String>>()
    val resetPassword = _resetPassword.asSharedFlow()

    init {
        getUser()
    }

    private fun getUser() {
        viewModelScope.launch {
            _user.emit(Resource.Loading())
            val user = getCollectionUseCase.execute("user") as User
            _user.emit(Resource.Success(user))
        }
    }


    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPassword.emit(Resource.Loading())
            val state = sendResetPasswordUseCase.execute(email)
            if (state == SUCCESS)
                _resetPassword.emit(Resource.Success(email))
            else
                _resetPassword.emit(Resource.Error(state))
        }
    }

    fun logout() {
        signOutUseCase.execute()
    }
}
