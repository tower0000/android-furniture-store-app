package com.example.koti.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.productsDatabaseUseCases.GetDatabaseProductsUseCase
import com.example.koti.domain.productsDatabaseUseCases.RefreshProductsUseCase
import com.example.koti.model.Product
import com.example.koti.ui.util.Constants.BEST_DEALS_CATEGORY
import com.example.koti.ui.util.Constants.FIELD_CATEGORY
import com.example.koti.ui.util.Constants.SHOP_PRODUCTS_COLLECTION
import com.example.koti.ui.util.Constants.SPECIAL_PRODUCTS_CATEGORY
import com.example.koti.ui.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.thread

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val refreshProductsUseCase: RefreshProductsUseCase,
    private val getDatabaseProductsUseCase: GetDatabaseProductsUseCase
) : ViewModel() {

    private val _specialProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val specialProducts: StateFlow<Resource<List<Product>>> = _specialProducts

    private val _bestDealsProducts =
        MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestDealsProducts: StateFlow<Resource<List<Product>>> = _bestDealsProducts

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts: StateFlow<Resource<List<Product>>> = _bestProducts

    private val pagingInfo = PagingInfo()

    init {
        fetchSpecialProducts()
        fetchBestDeals()
        fetchBestProducts()
        downloadItems()

    }

    fun fetchSpecialProducts() {
        if (!pagingInfo.isSpecialOffersPagingEnd) {
            viewModelScope.launch {
                _specialProducts.emit(Resource.Loading())
                firestore.collection(SHOP_PRODUCTS_COLLECTION)
                    .whereEqualTo(FIELD_CATEGORY, SPECIAL_PRODUCTS_CATEGORY)
                    .limit(pagingInfo.bestProductsPage * 2).get()
                    .addOnSuccessListener { result ->
                        val specialOffers = result.toObjects(Product::class.java)
                        pagingInfo.isSpecialOffersPagingEnd =
                            specialOffers == pagingInfo.oldSpecialOffers
                        pagingInfo.oldSpecialOffers = specialOffers
                        viewModelScope.launch {
                            _specialProducts.emit(Resource.Success(specialOffers))
                        }
                        pagingInfo.specialOffersPage++
                    }.addOnFailureListener {
                        viewModelScope.launch {
                            _specialProducts.emit(Resource.Error(it.message.toString()))
                        }
                    }
            }
        }
    }

    fun fetchBestDeals() {
        if (!pagingInfo.isDealsPagingEnd) {
            viewModelScope.launch {
                _bestDealsProducts.emit(Resource.Loading())
                firestore.collection(SHOP_PRODUCTS_COLLECTION)
                    .whereEqualTo(FIELD_CATEGORY, BEST_DEALS_CATEGORY)
                    .limit(pagingInfo.bestProductsPage * 2).get()
                    .addOnSuccessListener { result ->
                        val bestDeals = result.toObjects(Product::class.java)
                        pagingInfo.isDealsPagingEnd = bestDeals == pagingInfo.oldBestDeals
                        pagingInfo.oldBestDeals = bestDeals
                        viewModelScope.launch {
                            _bestDealsProducts.emit(Resource.Success(bestDeals))
                        }
                        pagingInfo.bestDealsPage++
                    }.addOnFailureListener {
                        viewModelScope.launch {
                            _bestDealsProducts.emit(Resource.Error(it.message.toString()))
                        }
                    }
            }
        }
    }


    fun fetchBestProducts() {
        if (!pagingInfo.isProductsPagingEnd) {
            viewModelScope.launch {
                _bestProducts.emit(Resource.Loading())
                firestore.collection(SHOP_PRODUCTS_COLLECTION)
                    .limit(pagingInfo.bestProductsPage * 6).get()
                    .addOnSuccessListener { result ->
                        val bestProducts = result.toObjects(Product::class.java)
                        pagingInfo.isProductsPagingEnd = bestProducts == pagingInfo.oldBestProducts
                        pagingInfo.oldBestProducts = bestProducts
                        viewModelScope.launch {
                            _bestProducts.emit(Resource.Success(bestProducts))
                        }
                        pagingInfo.bestProductsPage++
                    }.addOnFailureListener {
                        viewModelScope.launch {
                            _bestProducts.emit(Resource.Error(it.message.toString()))
                        }
                    }
            }
        }
    }

    fun downloadItems() {
        viewModelScope.launch { refreshProductsUseCase.execute() }
    }

    fun getDataBaseProducts() {
        viewModelScope.launch { getDatabaseProductsUseCase.execute() }
    }

}

internal data class PagingInfo(
    var bestProductsPage: Long = 1,
    var oldBestProducts: List<Product> = emptyList(),
    var isProductsPagingEnd: Boolean = false,

    var bestDealsPage: Long = 1,
    var oldBestDeals: List<Product> = emptyList(),
    var isDealsPagingEnd: Boolean = false,

    var specialOffersPage: Long = 1,
    var oldSpecialOffers: List<Product> = emptyList(),
    var isSpecialOffersPagingEnd: Boolean = false
)