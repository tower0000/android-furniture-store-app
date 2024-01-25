package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.firebaseUseCases.DeleteUserAddressUseCase
import com.example.koti.domain.firebaseUseCases.GetUserAddressesUseCase
import com.example.koti.domain.firebaseUseCases.PlaceOrderUseCase
import com.example.koti.model.Address
import com.example.koti.model.Order
import com.example.koti.ui.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val placerOrderUseCase: PlaceOrderUseCase,
    private val getUserAddressesUseCase: GetUserAddressesUseCase,
    private val deleteUserAddressUseCase: DeleteUserAddressUseCase
) : ViewModel() {

    private var addressCollectionDocuments = emptyList<DocumentSnapshot>()

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
            getUserAddressesUseCase.execute { doc, addresses, exception ->
                viewModelScope.launch {
                    if (exception != null)
                        _address.emit(Resource.Error(exception.message.toString()))
                    else
                        addressCollectionDocuments = doc!!
                        _address.emit(Resource.Success(addresses!!))
                }
            }
        }
    }

    fun placeOrder(order: Order) {
        viewModelScope.launch {
            _order.emit(Resource.Loading())
            placerOrderUseCase.execute(order) { exception ->
                viewModelScope.launch {
                    if (exception != null)
                        _order.emit(Resource.Error(exception.message.toString()))
                    else
                        _order.emit(Resource.Success(order))
                }
            }
        }
    }

    fun deleteUserAddress(userAddress: Address) {
        viewModelScope.launch {
            val index = address.value.data?.indexOf(userAddress)
            if (index != null && index != -1) {
                val documentId = addressCollectionDocuments[index].id
                deleteUserAddressUseCase.execute(documentId)
            }
        }
    }
}