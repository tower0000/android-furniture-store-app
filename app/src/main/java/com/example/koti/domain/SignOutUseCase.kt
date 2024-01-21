package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class SignOutUseCase @Inject constructor (private val repo: FirebaseRepository) {
    fun execute() = repo.signOut()
}