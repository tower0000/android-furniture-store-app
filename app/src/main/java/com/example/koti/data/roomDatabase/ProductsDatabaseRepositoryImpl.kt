package com.example.koti.data.roomDatabase

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.koti.domain.repository.ProductsDatabaseRepository
import com.example.koti.data.model.Product
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

) : ProductsDatabaseRepository {


    override suspend fun getProducts(): List<Product> {
        return dao.getProductsFromRoom()
    }

    override suspend fun insertProducts(products: List<Product>) {
        dao.insertProductsToRoom(products)
    }

    override suspend fun deleteAllProducts() {
        dao.deleteAllProducts()
    }

    override suspend fun getElementsCount(): Int {
        return dao.getElementsCount()
    }

}