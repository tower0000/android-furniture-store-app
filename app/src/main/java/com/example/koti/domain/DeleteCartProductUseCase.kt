package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class DeleteCartProductUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(documentId: String): Any = repo.deleteCartProduct(documentId)
}