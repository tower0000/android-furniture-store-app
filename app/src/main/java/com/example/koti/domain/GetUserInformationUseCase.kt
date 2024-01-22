package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class GetUserInformationUseCase @Inject constructor (val repository: FirebaseRepository){
    suspend fun execute() : Any {
        return repository.getUserInformation()
    }
}