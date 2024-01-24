package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Order
import com.example.koti.model.User
import javax.inject.Inject

class SaveUserInfoUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(userUid: String, user: User) = repo.saveUserInformation(userUid, user)
}