package com.example.koti.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.firebaseUseCases.GetAllProductsUseCase
import com.example.koti.domain.firebaseUseCases.GetServerItemsCount
import com.example.koti.domain.productsDatabaseUseCases.DeleteDatabaseProductsUseCase
import com.example.koti.domain.productsDatabaseUseCases.GetDatabaseElementsCount
import com.example.koti.domain.productsDatabaseUseCases.GetDatabaseProductsUseCase
import com.example.koti.domain.productsDatabaseUseCases.InsertProductsUseCase
import com.example.koti.model.Product
import com.example.koti.ui.util.Constants
import com.example.koti.ui.util.Constants.BEST_DEALS_CATEGORY
import com.example.koti.ui.util.Constants.BEST_PRODUCTS_KEY
import com.example.koti.ui.util.Constants.FIELD_CATEGORY
import com.example.koti.ui.util.Constants.SHOP_PRODUCTS_COLLECTION
import com.example.koti.ui.util.Constants.SPECIAL_PRODUCTS_CATEGORY
import com.example.koti.ui.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val firestore: FirebaseFirestore,
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val getDatabaseProductsUseCase: GetDatabaseProductsUseCase,
    private val insertProductsUseCase: InsertProductsUseCase,
    private val getDatabaseElementsCount: GetDatabaseElementsCount,
    private val getServerItemsCount: GetServerItemsCount
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
        checkForProductUpdates()
        if (!sharedPreferences.getBoolean(BEST_PRODUCTS_KEY, false)) {
            downloadBestProducts(pagingInfo.maxElementsOnPage.toLong() / 2)
        }
        showNewBestProductsState()
    }

    fun fetchSpecialProducts() {
        if (!pagingInfo.isSpecialOffersPagingEnd) {
            viewModelScope.launch {
                _specialProducts.emit(Resource.Loading())
                firestore.collection(SHOP_PRODUCTS_COLLECTION)
                    .whereEqualTo(FIELD_CATEGORY, SPECIAL_PRODUCTS_CATEGORY)
                    .limit(pagingInfo.specialOffersPage * 2).get()
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
                    .limit(pagingInfo.bestDealsPage * 2).get()
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
        if (!sharedPreferences.getBoolean(BEST_PRODUCTS_KEY, false)) {
            if (!pagingInfo.isProductsPagingEnd) {
                viewModelScope.launch {
                    _bestProducts.emit(Resource.Loading())

                    if (pagingInfo.bestProductsPage * pagingInfo.maxElementsOnPage > getDatabaseElementsCount.execute())
                        downloadBestProducts(pagingInfo.bestProductsPage * pagingInfo.maxElementsOnPage)

                    val products = getDatabaseProductsUseCase.execute()
                    _bestProducts.emit(Resource.Success(products))

                    pagingInfo.isProductsPagingEnd =
                        products.sortedBy { it.id } == pagingInfo.oldBestProducts.sortedBy { it.id }
                    pagingInfo.oldBestProducts = products
                    pagingInfo.bestProductsPage++
                }
            } else {
                sharedPreferences.edit().putBoolean(BEST_PRODUCTS_KEY, true).apply()
            }
        }
    }

    private fun downloadBestProducts(limit: Long) {
        viewModelScope.launch {
            getAllProductsUseCase.execute(limit) { res, e ->
                viewModelScope.launch {
                    if (e != null)
                        _bestProducts.emit(Resource.Error(e.message.toString()))
                    else {
                        insertProductsUseCase.execute(res!!)
                    }
                }
            }
        }
    }

    private fun showNewBestProductsState() {
        viewModelScope.launch {
            val products = getDatabaseProductsUseCase.execute()
            _bestProducts.emit(Resource.Success(products))
        }
    }

    private fun checkForProductUpdates() {
        viewModelScope.launch {
            val dbProductsCount = getDatabaseProductsUseCase.execute().count()
            getServerItemsCount.execute() { size ->
                if (size!! > dbProductsCount) {
                    viewModelScope.launch {
                        sharedPreferences.edit().putBoolean(Constants.BEST_PRODUCTS_KEY, false)
                            .apply()
                    }
                }
            }

        }
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
    var isSpecialOffersPagingEnd: Boolean = false,

    val maxElementsOnPage: Int = 8
)