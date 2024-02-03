package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.domain.model.Product
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject

class GetUserFavoritesUseCase @Inject constructor(private val repo: FirebaseRepository) {
    suspend fun execute(onResult: (List<DocumentSnapshot>?, List<Product>?, Exception?) -> Unit) =
        repo.getUserFavorites(onResult)
}