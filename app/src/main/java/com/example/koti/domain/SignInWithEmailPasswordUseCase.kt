package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class SignInWithEmailPasswordUseCase @Inject constructor(val repository: FirebaseRepository) {
    suspend fun execute(email: String, password: String, onResult: (Exception?) -> Unit) =
        repository.signInWithEmailPassword(email, password, onResult)
}