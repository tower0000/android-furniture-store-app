package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.model.Category
import com.example.koti.model.Product
import com.example.koti.ui.util.Constants.CATEGORY_COLLECTION
import com.example.koti.ui.util.Constants.PRODUCTS_COLLECTION
import com.example.koti.ui.util.Constants.SHOP_PRODUCTS_COLLECTION
import com.example.koti.ui.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryViewModel(
    private val store: FirebaseFirestore,
    private val category: Category
) : ViewModel() {

    private val _offerProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val offerProducts = _offerProducts.asStateFlow()

    init {
        fetchOfferProducts()
    }

    private fun fetchOfferProducts() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _offerProducts.emit(Resource.Loading())
                store.collection(SHOP_PRODUCTS_COLLECTION)
                    .whereEqualTo(CATEGORY_COLLECTION, category.category)
                    .whereNotEqualTo("offerPercentage", null).get()
                    .addOnSuccessListener {
                        val products = it.toObjects(Product::class.java)
                        viewModelScope.launch {
                            _offerProducts.emit(Resource.Success(products))
                        }
                    }.addOnFailureListener {
                        viewModelScope.launch {
                            _offerProducts.emit(Resource.Error(it.message.toString()))
                        }
                    }
            }
        }

    }
}