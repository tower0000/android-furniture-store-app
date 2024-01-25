package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class DeleteUserAddressUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(documentId: String): Any = repo.deleteUserAddress(documentId)
}