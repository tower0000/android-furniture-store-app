package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Address
import com.example.koti.model.CartProduct
import com.example.koti.model.Order
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject

class GetUserCartProductsUseCase @Inject constructor(private val repo: FirebaseRepository) {
    suspend fun execute(onResult: (List<DocumentSnapshot>?, List<CartProduct>?, Exception?) -> Unit) =
        repo.getUserCartProducts(onResult)
}