package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.data.model.Product
import com.example.koti.ui.util.Constants
import com.example.koti.ui.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(private val store: FirebaseFirestore) : ViewModel() {

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts: StateFlow<Resource<List<Product>>> = _bestProducts

    fun getResultProducts(query: String) {
        val uppercaseQuery = query.uppercase()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _bestProducts.emit(Resource.Loading())
                store.collection(Constants.SHOP_PRODUCTS_COLLECTION)
                    .whereGreaterThanOrEqualTo("name", uppercaseQuery)
                    .whereLessThanOrEqualTo("name", uppercaseQuery + "\uf8ff")
                    .get()
                    .addOnSuccessListener { docs ->
                        val products = docs.toObjects(Product::class.java)
                        viewModelScope.launch {
                            _bestProducts.emit(Resource.Success(products))
                        }
                    }
            }
        }

    }
}