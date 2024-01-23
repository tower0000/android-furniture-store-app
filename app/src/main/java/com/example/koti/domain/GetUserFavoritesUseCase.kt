package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Address
import com.example.koti.model.CartProduct
import com.example.koti.model.Order
import com.example.koti.model.Product
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject

class GetUserFavoritesUseCase @Inject constructor(private val repo: FirebaseRepository) {
    suspend fun execute(onResult: (List<DocumentSnapshot>?, List<Product>?, Exception?) -> Unit) =
        repo.getUserFavorites(onResult)
}