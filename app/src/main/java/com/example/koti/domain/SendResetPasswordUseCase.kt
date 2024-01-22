package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class SendResetPasswordUseCase @Inject constructor (val repository: FirebaseRepository){
    suspend fun execute(email : String): String = repository.sendPasswordReset(email)
}