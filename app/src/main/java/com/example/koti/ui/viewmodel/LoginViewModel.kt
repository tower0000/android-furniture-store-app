package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.SendResetPasswordUseCase
import com.example.koti.domain.SignInWithEmailPasswordUseCase
import com.example.koti.ui.util.Constants.SUCCESS
import com.example.koti.ui.util.RegisterFieldsState
import com.example.koti.ui.util.RegisterValidation
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.validateEmail
import com.example.koti.ui.util.validatePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val singInWithEmailPasswordUseCase: SignInWithEmailPasswordUseCase,
    private val sendResetPasswordUseCase: SendResetPasswordUseCase

) : ViewModel() {

    private val _login = MutableSharedFlow<Resource<String>>()
    val login = _login.asSharedFlow()

    private val _resetPassword = MutableSharedFlow<Resource<String>>()
    val resetPassword = _resetPassword.asSharedFlow()

    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (checkValidation(email, password)) {
                _login.emit(Resource.Loading())
                val state = singInWithEmailPasswordUseCase.execute(email, password)
                if (state == SUCCESS)
                    _login.emit(Resource.Success(email))
                else
                    _login.emit(Resource.Error(state))
            } else {
                val registerFieldsState = RegisterFieldsState(
                    validateEmail(email),
                    validatePassword(password)
                )
                _validation.send(registerFieldsState)
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPassword.emit(Resource.Loading())
            val requestState = sendResetPasswordUseCase.execute(email)
            if (requestState == SUCCESS)
                _resetPassword.emit(Resource.Success(email))
            else
                _resetPassword.emit(Resource.Error(requestState))
        }
    }

    private fun checkValidation(email: String, password: String): Boolean {
        val emailValidation = validateEmail(email)
        val passwordValidation = validatePassword(password)
        return emailValidation is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success
    }
}
