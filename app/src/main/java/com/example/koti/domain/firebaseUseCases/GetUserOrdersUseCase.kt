package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Order
import javax.inject.Inject

class GetUserOrdersUseCase @Inject constructor(private val repo: FirebaseRepository) {
    suspend fun execute(onResult: (List<Order>?, Exception?) -> Unit) = repo.getOrders(onResult)
}