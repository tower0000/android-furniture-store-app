package com.example.koti.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.R
import com.example.koti.domain.GetCurrentUserUseCase
import com.example.koti.ui.util.Constants.INTRODUCTION_KEY
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroductionViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _navigate = MutableStateFlow(0)
    val navigate: StateFlow<Int> = _navigate

    companion object {
        const val SHOPPING_ACTIVITY = 1
        val ACCOUNT_LOGIN = R.id.action_introductionFragment_to_loginFragment
    }

    init {
        viewModelScope.launch {
            val isButtonClicked = sharedPreferences.getBoolean(INTRODUCTION_KEY, false)
            val user = getCurrentUserUseCase.execute()
            if (user != null) {
                _navigate.emit(SHOPPING_ACTIVITY)
            } else if (isButtonClicked) {
                _navigate.emit(ACCOUNT_LOGIN)
            } else {
                Unit
            }
        }
    }

    fun startButtonClick() {
        sharedPreferences.edit().putBoolean(INTRODUCTION_KEY, true).apply()
    }

}