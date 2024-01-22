package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.User
import javax.inject.Inject


class SignUpWithEmailPasswordUseCase @Inject constructor (val repository: FirebaseRepository) {
    suspend fun execute(user: User, password: String): String {
        return repository.signUpWithEmailPassword(user, password)
    }
}