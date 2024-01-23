package com.example.koti.domain

import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.CartProduct
import com.example.koti.model.Product
import javax.inject.Inject

class AddProductToFavoritesUseCase @Inject constructor (private val repo: FirebaseRepository) {
    suspend fun execute(favProduct: Product, onResult: (Exception?) -> Unit) = repo.addProductToFavorites(favProduct, onResult)
}