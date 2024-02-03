package com.example.koti.ui.viewmodel
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.firebaseUseCases.GetUserInformationUseCase
import com.example.koti.domain.firebaseUseCases.SendResetPasswordUseCase
import com.example.koti.domain.firebaseUseCases.SignOutUseCase
import com.example.koti.domain.productsDatabaseUseCases.DeleteDatabaseProductsUseCase
import com.example.koti.domain.model.User
import com.example.koti.ui.util.Constants
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
    private val sharedPreferences: SharedPreferences,
    private val sendResetPasswordUseCase: SendResetPasswordUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val deleteDatabaseProductsUseCase: DeleteDatabaseProductsUseCase
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
            getUserInformationUseCase.execute { user, exception ->
                viewModelScope.launch {
                    if(exception != null)
                        _user.emit(Resource.Error(exception.message.toString()))
                    else
                        _user.emit(Resource.Success(user!!))
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPassword.emit(Resource.Loading())
            sendResetPasswordUseCase.execute(email) { exception ->
                launch {
                    if (exception != null)
                        _resetPassword.emit(Resource.Error(exception.message.toString()))
                    else
                        _resetPassword.emit(Resource.Success(email))
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            signOutUseCase.execute()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            deleteDatabaseProductsUseCase.execute()
            sharedPreferences.edit().putBoolean(Constants.BEST_PRODUCTS_KEY, false).apply()
        }
    }

}
