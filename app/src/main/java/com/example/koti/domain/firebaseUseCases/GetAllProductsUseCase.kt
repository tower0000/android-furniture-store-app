package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.data.model.Product
import com.example.koti.data.model.User
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(val repository: FirebaseRepository) {
    suspend fun execute(limit: Long, onResult: (List<Product>?, Exception?) -> Unit) {
        return repository.getAllProducts(limit, onResult)
    }
}