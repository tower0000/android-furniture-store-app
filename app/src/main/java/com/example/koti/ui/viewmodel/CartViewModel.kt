package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.ChangeCartProductQuantityUseCase
import com.example.koti.domain.GetUserCartProductsUseCase
import com.example.koti.model.CartProduct
import com.example.koti.model.QuantityChanging
import com.example.koti.ui.util.FirebaseCommon
import com.example.koti.ui.util.getProductPrice
import com.example.koti.ui.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val getUserCartProductsUseCase: GetUserCartProductsUseCase,
    private val changeCartProductQuantityUseCase: ChangeCartProductQuantityUseCase
) : ViewModel() {

    private val _cartProducts =
        MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())
    val cartProducts = _cartProducts.asStateFlow()

    val productsPrice = cartProducts.map {
        when (it) {
            is Resource.Success -> {
                calculatePrice(it.data!!)
            }

            else -> null
        }
    }

    private var cartProductDocuments = emptyList<DocumentSnapshot>()
    private val _deleteDialog = MutableSharedFlow<CartProduct>()
    val deleteDialog = _deleteDialog.asSharedFlow()
    fun deleteCartProduct(cartProduct: CartProduct) {
        val index = cartProducts.value.data?.indexOf(cartProduct)
        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("cart")
                .document(documentId).delete()
        }
    }

    private fun calculatePrice(data: List<CartProduct>): Float {
        return data.sumByDouble { cartProduct ->
            (cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price) * cartProduct.quantity).toDouble()
        }.toFloat()
    }

    init {
        getCartProducts()
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

}