package com.example.koti.domain.productsDatabaseUseCases

import com.example.koti.domain.repository.ProductsDatabaseRepository
import com.example.koti.domain.model.Product
import javax.inject.Inject

class DeleteDatabaseProductsUseCase @Inject constructor(private val repo: ProductsDatabaseRepository) {
    suspend fun execute() = repo.deleteAllProducts()
}