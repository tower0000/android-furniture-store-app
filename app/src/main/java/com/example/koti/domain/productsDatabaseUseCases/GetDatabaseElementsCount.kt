package com.example.koti.domain.productsDatabaseUseCases

import com.example.koti.domain.repository.ProductsDatabaseRepository
import javax.inject.Inject

class GetDatabaseElementsCount @Inject constructor(private val repo: ProductsDatabaseRepository) {
    suspend fun execute(): Int = repo.getElementsCount()
}