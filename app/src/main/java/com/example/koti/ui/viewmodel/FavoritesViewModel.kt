package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.model.CartProduct
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _favoriteProducts =
        MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())
    val favoriteProducts = _favoriteProducts.asStateFlow()

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    private var favProductDocuments = emptyList<DocumentSnapshot>()

    private val _deleteDialog = MutableSharedFlow<CartProduct>()
    val deleteDialog = _deleteDialog.asSharedFlow()

    private val _addDialog = MutableSharedFlow<CartProduct>()
    val addDialog = _addDialog.asSharedFlow()

    fun deleteFavProduct(favsProduct: CartProduct) {
        val index = favoriteProducts.value.data?.indexOf(favsProduct)
        if (index != null && index != -1) {
            val documentId = favProductDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("favorites")
                .document(documentId).delete()
        }
    }

    fun addFavToCart(cartProduct: CartProduct) {
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
            firestore.collection("user").document(auth.uid!!).collection("favorites")
                .addSnapshotListener { value, error ->
                    if (error != null || value == null) {
                        viewModelScope.launch { _favoriteProducts.emit(Resource.Error(error?.message.toString())) }
                    } else {
                        favProductDocuments = value.documents
                        val cartProducts = value.toObjects(CartProduct::class.java)
                        viewModelScope.launch { _favoriteProducts.emit(Resource.Success(cartProducts)) }
                    }
                }
        }
    }

    fun changeQuantity(
        favsProduct: CartProduct,
        quantityChanging: FirebaseCommon.QuantityChanging
    ) {
        val index = favoriteProducts.value.data?.indexOf(favsProduct)

        if (index != null && index != -1) {
            val documentId = favProductDocuments[index].id
            when (quantityChanging) {
                FirebaseCommon.QuantityChanging.INCREASE -> {
                    if (favsProduct.quantity == 1) {
                        viewModelScope.launch { _addDialog.emit(favsProduct) }
                        return
                    }
                    viewModelScope.launch { _favoriteProducts.emit(Resource.Loading()) }
                    decreaseQuantity(documentId)
                }

                FirebaseCommon.QuantityChanging.DECREASE -> {
                    if (favsProduct.quantity == 1) {
                        viewModelScope.launch { _deleteDialog.emit(favsProduct) }
                        return
                    }
                    viewModelScope.launch { _favoriteProducts.emit(Resource.Loading()) }
                    decreaseQuantity(documentId)
                }

            }
        }
    }

    private fun decreaseQuantity(documentId: String) {
        firebaseCommon.decreaseQuantity(documentId) { result, exception ->
            if (exception != null)
                viewModelScope.launch { _favoriteProducts.emit(Resource.Error(exception.message.toString())) }
        }
    }

}