package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import javax.inject.Inject

class GetServerItemsCount @Inject constructor(val repository: FirebaseRepository) {
    suspend fun execute(onResult: (Int?) -> Unit) {
        return repository.getServerProductsCount(onResult)
    }
}