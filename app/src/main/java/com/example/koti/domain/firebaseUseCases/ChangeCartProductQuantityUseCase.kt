package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.domain.model.QuantityChanging
import javax.inject.Inject

class ChangeCartProductQuantityUseCase @Inject constructor(val repository: FirebaseRepository) {
    suspend fun execute(
        quantityChanging: QuantityChanging,
        documentId: String,
        onResult: (Exception?) -> Unit
    ) {
        return when (quantityChanging) {
            QuantityChanging.DECREASE -> repository.decreaseQuantity(documentId, onResult)
            QuantityChanging.INCREASE -> repository.increaseQuantity(documentId, onResult)
        }
    }
}