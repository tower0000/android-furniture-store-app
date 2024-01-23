package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.ChangeCartProductQuantityUseCase
import com.example.koti.domain.DeleteCartProductUseCase
import com.example.koti.domain.GetUserCartProductsUseCase
import com.example.koti.model.CartProduct
import com.example.koti.model.QuantityChanging
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.getProductPrice
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getUserCartProductsUseCase: GetUserCartProductsUseCase,
    private val changeCartProductQuantityUseCase: ChangeCartProductQuantityUseCase,
    private val deleteCartProductUseCase: DeleteCartProductUseCase
) : ViewModel() {

    private val _cartProducts =
        MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())
    val cartProducts = _cartProducts.asStateFlow()
    private var cartProductDocuments = emptyList<DocumentSnapshot>()
    private val _deleteDialog = MutableSharedFlow<CartProduct>()
    val deleteDialog = _deleteDialog.asSharedFlow()
    val productsPrice = cartProducts.map {
        when (it) {
            is Resource.Success -> {
                calculatePrice(it.data!!)
            }

            else -> null
        }
    }


    init {
        getCartProducts()
    }

    fun deleteCartProduct(cartProduct: CartProduct) {
        viewModelScope.launch {
            val index = cartProducts.value.data?.indexOf(cartProduct)
            if (index != null && index != -1) {
                val documentId = cartProductDocuments[index].id
                deleteCartProductUseCase.execute(documentId)
            }
        }
    }

    private fun getCartProducts() {
        viewModelScope.launch {
            _cartProducts.emit(Resource.Loading())
            getUserCartProductsUseCase.execute { docs, products, exception ->
                viewModelScope.launch {
                    if (exception != null) {
                        _cartProducts.emit(Resource.Error(exception.message.toString()))
                    } else {
                        cartProductDocuments = docs!!
                        _cartProducts.emit(Resource.Success(products!!))
                    }
                }
            }
        }
    }

    fun changeQuantity(
        cartProduct: CartProduct,
        quantityChanging: QuantityChanging
    ) {
        viewModelScope.launch {
            val index = cartProducts.value.data?.indexOf(cartProduct)
            if (index != null && index != -1) {
                val documentId = cartProductDocuments[index].id
                when (quantityChanging) {

                    QuantityChanging.INCREASE -> {
                        _cartProducts.emit(Resource.Loading())
                        changeCartProductQuantityUseCase.execute(
                            QuantityChanging.INCREASE,
                            documentId
                        ) { exception ->
                            viewModelScope.launch {
                                if (exception != null)
                                    _cartProducts.emit(Resource.Error(exception.message.toString()))
                            }
                        }
                    }

                    QuantityChanging.DECREASE -> {
                        if (cartProduct.quantity == 1) {
                            _deleteDialog.emit(cartProduct)
                        } else {
                            _cartProducts.emit(Resource.Loading())
                            changeCartProductQuantityUseCase.execute(
                                QuantityChanging.DECREASE,
                                documentId
                            ) { exception ->
                                viewModelScope.launch {
                                    if (exception != null)
                                        _cartProducts.emit(Resource.Error(exception.message.toString()))
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun calculatePrice(data: List<CartProduct>): Float {
        return data.sumOf { cartProduct ->
            (cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price) * cartProduct.quantity).toDouble()
        }.toFloat()
    }

}