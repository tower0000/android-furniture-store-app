package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject


class SignUpWithEmailPasswordUseCase (private val repo: FirebaseRepository) {
    suspend fun signUpWithEmailPassword(email: String , password: String): FirebaseUser? {
        return repo.signUpWithEmailPassword(email, password)
    }
}