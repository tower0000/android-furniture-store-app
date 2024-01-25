package com.example.koti.domain.firebaseUseCases

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.CartProduct
import javax.inject.Inject

class AddProductToCartUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(cartProduct: CartProduct, onResult: (Exception?) -> Unit) = repo.addNewProductToCart(cartProduct, onResult)
}