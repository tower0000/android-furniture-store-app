package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.AddProductToCartUseCase
import com.example.koti.domain.AddProductToFavoritesUseCase
import com.example.koti.domain.ChangeCartProductQuantityUseCase
import com.example.koti.domain.DeleteFavoritesProductUseCase
import com.example.koti.domain.GetUserFavoritesUseCase
import com.example.koti.model.CartProduct
import com.example.koti.model.Product
import com.example.koti.model.QuantityChanging
import com.example.koti.ui.util.Constants.CART_COLLECTION
import com.example.koti.ui.util.Constants.FAVORITES_COLLECTION
import com.example.koti.ui.util.Constants.USER_COLLECTION
import com.example.koti.ui.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val store: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val changeCartProductQuantityUseCase: ChangeCartProductQuantityUseCase,
    private val addProductToCartUseCase: AddProductToCartUseCase,
    private val addProductToFavoritesUseCase: AddProductToFavoritesUseCase,
    private val deleteFavoritesProductUseCase: DeleteFavoritesProductUseCase,
    private val getUserFavoritesUseCase: GetUserFavoritesUseCase

) : ViewModel() {
    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    private val _addDeleteProductFavorites =
        MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addDeleteProductFavorites = _addDeleteProductFavorites.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    private var favProductDocuments = emptyList<DocumentSnapshot>()
    private var favProducts = emptyList<Product>()

    fun checkIfProductInFavorites(favProduct: Product) {
        viewModelScope.launch {
            getUserFavoritesUseCase.execute { doc, fav, exception ->
                viewModelScope.launch {
                    if (exception == null) {
                        favProducts = fav!!
                        favProductDocuments = doc!!
                        _isFavorite.emit(false)
                        for (each in favProducts) {
                            if(favProducts.contains(favProduct)) {
                                _isFavorite.emit(true)
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    fun addUpdateProductsInCart(cartProduct: CartProduct) {
        viewModelScope.launch {
            _addToCart.emit(Resource.Loading())
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION)
                .whereEqualTo("product.id", cartProduct.product.id).get()
                .addOnSuccessListener { it ->
                    it.documents.let {
                        if (it.isEmpty()) {
                            addNewProductToCart(cartProduct)
                        } else {
                            viewModelScope.launch {
                                val product = it.first().toObject(CartProduct::class.java)
                                if (product == cartProduct) {
                                    val documentId = it.first().id
                                    changeCartProductQuantityUseCase.execute(
                                        QuantityChanging.INCREASE,
                                        documentId
                                    ) { exception ->
                                        viewModelScope.launch {
                                            if (exception != null)
                                                _addToCart.emit(Resource.Error(exception.message.toString()))
                                        }
                                    }
                                } else {
                                    addNewProductToCart(cartProduct)
                                }
                            }
                        }
                    }
                }.addOnFailureListener {
                    viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
                }
        }
    }

    fun addDeleteFavoriteProduct(favProduct: Product) {
        viewModelScope.launch { _addDeleteProductFavorites.emit(Resource.Loading()) }
        store.collection(USER_COLLECTION).document(auth.uid!!).collection(FAVORITES_COLLECTION)
            .whereEqualTo("id", favProduct.id).get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()) {
                        addNewProductToFavorites(favProduct)
                    } else {
                        viewModelScope.launch {
                            val index = favProducts.indexOf(favProduct)
                            val product = it.first().toObject(Product::class.java)
                            if (product == favProduct) {
                                val documentId = favProductDocuments[index].id
                                deleteFavoritesProductUseCase.execute(documentId)
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _addDeleteProductFavorites.emit(Resource.Error(it.message.toString())) }
            }
    }




    private fun addNewProductToFavorites(favProduct: Product) {
        viewModelScope.launch {
            addProductToFavoritesUseCase.execute(favProduct) { exception ->
                viewModelScope.launch {
                    if (exception != null)
                        _addDeleteProductFavorites.emit(Resource.Error(exception.message.toString()))
                }
            }
        }
    }

    private fun addNewProductToCart(cartProduct: CartProduct) {
        viewModelScope.launch {
            addProductToCartUseCase.execute(cartProduct) { exception ->
                viewModelScope.launch {
                    if (exception != null)
                        _addToCart.emit(Resource.Error(exception.message.toString()))
                    else
                        _addToCart.emit(Resource.Success(cartProduct))
                }
            }
        }
    }

}