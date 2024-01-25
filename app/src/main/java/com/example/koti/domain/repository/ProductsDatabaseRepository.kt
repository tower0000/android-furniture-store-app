package com.example.koti.domain.repository

import com.example.koti.model.Product
import kotlinx.coroutines.flow.MutableStateFlow

interface ProductsDatabaseRepository {
    suspend fun getProducts(): List<Product>
    suspend fun refreshProducts()

}