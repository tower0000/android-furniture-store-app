package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.firebaseUseCases.GetUserOrdersUseCase
import com.example.koti.data.model.Order
import com.example.koti.ui.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllOrdersViewModel @Inject constructor(
    private val getUserOrdersUseCase: GetUserOrdersUseCase
) : ViewModel() {

    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()

    init {
        getAllOrders()
    }

    private fun getAllOrders() {
        viewModelScope.launch {
            _allOrders.emit(Resource.Loading())
            getUserOrdersUseCase.execute { orders, exception ->
                viewModelScope.launch {
                    if (exception != null)
                        _allOrders.emit(Resource.Error(exception.message.toString()))
                    else
                        _allOrders.emit(Resource.Success(orders!!))
                }
            }
        }
    }
}
