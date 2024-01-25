package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.firebaseUseCases.AddProductToCartUseCase
import com.example.koti.domain.firebaseUseCases.ChangeCartProductQuantityUseCase
import com.example.koti.domain.firebaseUseCases.DeleteFavoritesProductUseCase
import com.example.koti.domain.firebaseUseCases.GetUserFavoritesUseCase
import com.example.koti.model.CartProduct
import com.example.koti.model.Product
import com.example.koti.model.QuantityChanging
import com.example.koti.ui.util.Constants.CART_COLLECTION
import com.example.koti.ui.util.Constants.USER_COLLECTION
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
    private val store: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val deleteFavoritesProductUseCase: DeleteFavoritesProductUseCase,
    private val getUserFavoritesUseCase: GetUserFavoritesUseCase,
    private val addProductToCartUseCase: AddProductToCartUseCase,
    private val changeCartProductQuantityUseCase: ChangeCartProductQuantityUseCase
) : ViewModel() {

    private val _favoriteProducts =
        MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val favoriteProducts = _favoriteProducts.asStateFlow()

    private val _addToCart = MutableStateFlow<Resource<Product>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    private val _deleteDialog = MutableSharedFlow<Product>()
    val deleteDialog = _deleteDialog.asSharedFlow()

    private val _addDialog = MutableSharedFlow<Product>()
    val addDialog = _addDialog.asSharedFlow()

    private var favProductDocuments = emptyList<DocumentSnapshot>()

    init {
        getFavorites()
    }

    private fun getFavorites() {
        viewModelScope.launch {
            getUserFavoritesUseCase.execute { doc, fav, exception ->
                viewModelScope.launch {
                    if (exception != null)
                        _favoriteProducts.emit(Resource.Error(exception.message.toString()))
                    else {
                        favProductDocuments = doc!!
                        _favoriteProducts.emit(Resource.Success(fav!!))
                    }
                }
            }
        }
    }

    fun changeFavoriteProductState(
        favProduct: Product,
        quantityChanging: QuantityChanging
    ) {
        viewModelScope.launch {
            when (quantityChanging) {
                QuantityChanging.INCREASE -> {
                    _addDialog.emit(favProduct)
                }

                QuantityChanging.DECREASE -> {
                    _deleteDialog.emit(favProduct)
                }
            }

        }
    }

    fun addFavToCart(favProduct: Product) {
        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }
        store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION)
            .whereEqualTo("product.id", favProduct.id).get()
            .addOnSuccessListener { it ->
                it.documents.let {
                    if (it.isEmpty()) {
                        createFavProductInCart(favProduct)
                    } else {
                        viewModelScope.launch {
                            val product = it.first().toObject(CartProduct::class.java)
                            if (product?.product == favProduct) {
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
                                createFavProductInCart(favProduct)
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
            }
    }

    private fun createFavProductInCart(favProduct: Product) {
        viewModelScope.launch {
            val fav2Cart = CartProduct(favProduct,1,null,null)
            addProductToCartUseCase.execute(fav2Cart) { exception ->
                viewModelScope.launch {
                    if (exception != null)
                        _addToCart.emit(Resource.Error(exception.message.toString()))
                    else
                        _addToCart.emit(Resource.Success(favProduct))
                }
            }
        }
    }

    fun deleteFavProduct(favProduct: Product) {
        viewModelScope.launch {
            val index = favoriteProducts.value.data?.indexOf(favProduct)
            if (index != null && index != -1) {
                val documentId = favProductDocuments[index].id
                deleteFavoritesProductUseCase.execute(documentId)
            }
        }
    }

}