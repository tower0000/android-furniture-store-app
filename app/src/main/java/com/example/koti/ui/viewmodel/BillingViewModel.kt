package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.GetUserAddressesUseCase
import com.example.koti.domain.PlaceOrderUseCase
import com.example.koti.model.Address
import com.example.koti.model.Order
import com.example.koti.ui.util.Constants.SUCCESS
import com.example.koti.ui.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val placerOrderUseCase: PlaceOrderUseCase,
    private val getUserAddressesUseCase: GetUserAddressesUseCase
) : ViewModel() {

    private val _address = MutableStateFlow<Resource<List<Address>>>(Resource.Unspecified())
    val address = _address.asStateFlow()

    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()

    init {
        getUserAddresses()
    }

    fun getUserAddresses() {
        viewModelScope.launch {
            _address.emit(Resource.Loading())
            val result = getUserAddressesUseCase.execute()
            if (result is String)
                _address.emit(Resource.Error(result))
            else
                _address.emit(Resource.Success(result as List<Address>))
        }
    }

    fun placeOrder(order: Order) {
        viewModelScope.launch {
            _order.emit(Resource.Loading())
            val result = placerOrderUseCase.execute(order)
            if (result == SUCCESS)
                _order.emit(Resource.Success(order))
            else
                _order.emit(Resource.Error(result))
        }
    }
}