package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.data.model.Order
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(order: Order, onResult: (Exception?) -> Unit) = repo.placeOrder(order, onResult)
}