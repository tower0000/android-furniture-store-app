package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.User
import javax.inject.Inject


class SignUpWithEmailPasswordUseCase @Inject constructor (val repository: FirebaseRepository) {
    suspend fun execute(user: User, password: String, onResult: (Exception?) -> Unit) =
        repository.signUpWithEmailPassword(user, password, onResult)
}