package com.example.koti.domain.productsDatabaseUseCases

import com.example.koti.domain.repository.ProductsDatabaseRepository
import com.example.koti.model.Product
import javax.inject.Inject

class InsertProductsUseCase @Inject constructor(private val repo: ProductsDatabaseRepository) {
    suspend fun execute(products: List<Product>) = repo.insertProducts(products)
}