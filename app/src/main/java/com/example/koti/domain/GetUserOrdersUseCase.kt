package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class GetUserOrdersUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(): Any = repo.getOrders()
}