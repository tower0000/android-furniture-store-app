package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Address
import javax.inject.Inject

class AddNewAddressUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(address: Address): String = repo.addNewAddress(address)
}