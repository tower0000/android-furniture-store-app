package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.ChangeCartProductQuantityUseCase
import com.example.koti.model.CartProduct
import com.example.koti.model.QuantityChanging
import com.example.koti.ui.util.FirebaseCommon
import com.example.koti.ui.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon,
    private val changeCartProductQuantityUseCase: ChangeCartProductQuantityUseCase

) : ViewModel() {
    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    private val _addToFavorites = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToFavorites = _addToFavorites.asStateFlow()

    fun addUpdateProductsInCart(cartProduct: CartProduct) {
        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart")
            .whereEqualTo("product.id", cartProduct.product.id).get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()) {
                        addNewProduct(cartProduct)
                    } else {
                        val product = it.first().toObject(CartProduct::class.java)
                        if (product == cartProduct) {
                            val documentId = it.first().id
                            increaseQuantity(documentId, cartProduct)
                        } else {
                            addNewProduct(cartProduct)
                        }
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
            }
    }

    private fun addNewProduct(cartProduct: CartProduct) {
        firebaseCommon.addProductToCart(cartProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(addedProduct!!))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }

        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct) {
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(cartProduct))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    fun addProductsToFavorites(favProduct: CartProduct) {
        viewModelScope.launch { _addToFavorites.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("favorites")
            .whereEqualTo("product.id", favProduct.product.id).get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()) {
                        addNewProductToFavorites(favProduct)
                    } else {
                        val product = it.first().toObject(CartProduct::class.java)
                        if (product == favProduct) {
                            viewModelScope.launch { _addToFavorites.emit(Resource.Error("Product was deleted from favorites")) }
//                            deleteProductFromFavorites(favProduct)
                        } else {
                            addNewProductToFavorites(favProduct)
                        }
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _addToFavorites.emit(Resource.Error(it.message.toString())) }
            }
    }

//    private fun deleteProductFromFavorites(favProduct: CartProduct) {
//        firebaseCommon.deleteProductFromFavorites(favProduct) { addedProduct, e ->
//            viewModelScope.launch {
//                if (e == null)
//                    _addToFavorites.emit(Resource.Success(addedProduct!!))
//                else
//                    _addToFavorites.emit(Resource.Error(e.message.toString()))
//            }
//
//        }
//    }

    private fun addNewProductToFavorites(favProduct: CartProduct) {
        firebaseCommon.addProductToFavorites(favProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToFavorites.emit(Resource.Success(addedProduct!!))
                else
                    _addToFavorites.emit(Resource.Error(e.message.toString()))
            }

        }
    }

}