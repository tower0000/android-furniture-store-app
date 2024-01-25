package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.firebaseUseCases.GetCurrentUserUseCase
import com.example.koti.domain.firebaseUseCases.SaveUserInfoUseCase
import com.example.koti.domain.firebaseUseCases.SignUpWithEmailPasswordUseCase
import com.example.koti.model.User
import com.example.koti.ui.util.RegisterFieldsState
import com.example.koti.ui.util.RegisterValidation
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.validateEmail
import com.example.koti.ui.util.validatePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val signUpWithEmailPasswordUseCase: SignUpWithEmailPasswordUseCase,
    private val saveUserInfoUseCase: SaveUserInfoUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _register = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> = _register

    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()
    fun createAccountWithEmailAndPassword(user: User, password: String) {
        viewModelScope.launch {
            if (checkValidation(user, password)) {
                _register.emit(Resource.Loading())
                signUpWithEmailPasswordUseCase.execute(user, password) { exception ->
                    viewModelScope.launch {
                        if (exception != null)
                            _register.emit(Resource.Error(exception.message.toString()))
                        else
                            _register.emit(Resource.Success(user))
                    }
                }
            } else {
                val registerFieldsState = RegisterFieldsState(
                    validateEmail(user.email),
                    validatePassword(password)
                )
                _validation.send(registerFieldsState)
            }
        }
    }

    private fun checkValidation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        return emailValidation is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success
    }

    fun saveUserData(name: String, email: String, imgUrl: String) {
        viewModelScope.launch {
            val loggedUser = getCurrentUserUseCase.execute()
            val userUid = loggedUser?.uid
            val user = User(name, "", email, imgUrl)
            saveUserInfoUseCase.execute(userUid!!,user)
        }
    }
}