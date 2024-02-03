package com.example.koti.data.roomDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.koti.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow

@Dao
interface ProductDao {
    @Query("select * from products_table")
    suspend fun getProductsFromRoom(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductsToRoom(products:List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(products: Product)

    @Query("DELETE FROM products_table")
    suspend fun deleteAllProducts()

    @Query("SELECT COUNT(*) FROM products_table")
    suspend fun getElementsCount(): Int
}