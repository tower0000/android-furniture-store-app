package com.example.koti.domain.productsDatabaseUseCases

import com.example.koti.domain.repository.ProductsDatabaseRepository
import com.example.koti.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class GetDatabaseProductsUseCase @Inject constructor(private val repo: ProductsDatabaseRepository) {
    suspend fun execute(): List<Product> = repo.getProducts()
}