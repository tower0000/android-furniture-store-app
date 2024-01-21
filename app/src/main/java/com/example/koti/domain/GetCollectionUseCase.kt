package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class GetCollectionUseCase @Inject constructor (val repository: FirebaseRepository){
    suspend fun execute(collectionName: String) : Any? {
        return repository.getCollection(collectionName)
    }
}