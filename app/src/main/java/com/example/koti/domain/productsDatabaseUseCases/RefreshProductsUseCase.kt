package com.example.koti.domain.productsDatabaseUseCases

import com.example.koti.domain.repository.ProductsDatabaseRepository
import javax.inject.Inject

class RefreshProductsUseCase @Inject constructor(private val repo: ProductsDatabaseRepository) {
    suspend fun execute() = repo.refreshProducts()
}