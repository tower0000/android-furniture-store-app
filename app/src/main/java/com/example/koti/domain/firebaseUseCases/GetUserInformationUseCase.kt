package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.User
import javax.inject.Inject

class GetUserInformationUseCase @Inject constructor (val repository: FirebaseRepository){
    suspend fun execute(onResult: (User?, Exception?) -> Unit) {
        return repository.getUserInformation(onResult)
    }
}