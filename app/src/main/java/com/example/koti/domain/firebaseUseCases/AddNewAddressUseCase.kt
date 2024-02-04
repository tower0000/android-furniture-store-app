package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.data.model.Address
import javax.inject.Inject

class AddNewAddressUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(address: Address, onResult: (Exception?) -> Unit) = repo.addNewAddress(address, onResult)
}