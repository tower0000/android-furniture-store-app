package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Address
import com.example.koti.model.Order
import javax.inject.Inject

class GetOrdersUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(): Any = repo.getOrders()
}