package com.example.koti.data.roomDatabase

import android.util.Log
import com.example.koti.domain.repository.ProductsDatabaseRepository
import com.example.koti.model.Product
import com.example.koti.ui.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductsDatabaseRepositoryImpl @Inject constructor(
    private val dao: ProductDao,
    private val store: FirebaseFirestore

) : ProductsDatabaseRepository {


    override suspend fun getProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            dao.getProductsFromRoom()
        }
    }

    override suspend fun refreshProducts() {
        withContext(Dispatchers.IO) {
            store.collection(Constants.SHOP_PRODUCTS_COLLECTION)
                .limit(5).get()
                .addOnSuccessListener { result ->
                    val products = result.toObjects(ProductEntity::class.java)
                    launch {
                        dao.insertProductsToRoom(products)
                    }

                }.addOnFailureListener {
                    Log.e("Room", it.message.toString())
                }

        }
    }
}