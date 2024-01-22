package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Order
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(order: Order): String = repo.placeOrder(order)
}