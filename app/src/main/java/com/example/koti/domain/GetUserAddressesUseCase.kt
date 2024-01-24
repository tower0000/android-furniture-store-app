package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Address
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject

class GetUserAddressesUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(onResult: (List<DocumentSnapshot>?, List<Address>?, Exception?) -> Unit) = repo.getUserAddresses(onResult)
}