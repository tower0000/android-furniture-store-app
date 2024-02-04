package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.firebaseUseCases.AddNewAddressUseCase
import com.example.koti.data.model.Address
import com.example.koti.ui.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addNewAddressUseCase: AddNewAddressUseCase
) : ViewModel() {

    private val _addNewAddress = MutableStateFlow<Resource<Address>>(Resource.Unspecified())
    val addNewAddress = _addNewAddress.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()
    fun addAddress(address: Address) {
        viewModelScope.launch {
            val validateInputs = validateInputs(address)
            if (validateInputs) {
                _addNewAddress.emit(Resource.Loading())
                addNewAddressUseCase.execute(address) { exception ->
                    viewModelScope.launch {
                        if (exception != null)
                            _addNewAddress.emit(Resource.Error(exception.message.toString()))
                        else
                            _addNewAddress.emit(Resource.Success(address))
                    }
                }
            } else {
                _error.emit("All fields are required")
            }
        }
    }

    private fun validateInputs(address: Address): Boolean {
        return address.addressTitle.isNotEmpty() &&
                address.city.trim().isNotEmpty() &&
                address.phone.trim().isNotEmpty() &&
                address.state.trim().isNotEmpty() &&
                address.fullName.trim().isNotEmpty() &&
                address.street.trim().isNotEmpty()
    }
}