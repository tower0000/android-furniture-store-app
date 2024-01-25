package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class SendResetPasswordUseCase @Inject constructor(val repository: FirebaseRepository) {
    suspend fun execute(email: String, onResult: (Exception?) -> Unit) =
        repository.sendPasswordReset(email, onResult)
}