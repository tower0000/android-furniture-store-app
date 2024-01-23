package com.example.koti.domain.repository

import com.example.koti.model.Address
import com.example.koti.model.CartProduct
import com.example.koti.model.Order
import com.example.koti.model.Product
import com.example.koti.model.QuantityChanging
import com.example.koti.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot

interface FirebaseRepository {

    suspend fun signUpWithEmailPassword(
        user: User,
        password: String,
        onResult: (Exception?) -> Unit
    )

    suspend fun signInWithEmailPassword(
        email: String,
        password: String,
        onResult: (Exception?) -> Unit
    )

    suspend fun sendPasswordReset(email: String, onResult: (Exception?) -> Unit)
    suspend fun getUserInformation(onResult: (User?, Exception?) -> Unit)
    suspend fun getCurrentUser(): FirebaseUser?
    suspend fun addNewAddress(address: Address, onResult: (Exception?) -> Unit)
    suspend fun addNewProductToCart(cartProduct: CartProduct, onResult: (Exception?) -> Unit)
    suspend fun addProductToFavorites(favProduct: Product, onResult: (Exception?) -> Unit)
    suspend fun getOrders(onResult: (List<Order>?, Exception?) -> Unit)
    suspend fun getUserAddresses(onResult: (List<Address>?, Exception?) -> Unit)
    suspend fun getUserCartProducts(onResult: (List<DocumentSnapshot>?, List<CartProduct>?, Exception?) -> Unit)
    suspend fun getUserFavorites(onResult: (List<DocumentSnapshot>?, List<Product>?, Exception?) -> Unit)
    suspend fun placeOrder(order: Order, onResult: (Exception?) -> Unit)
    suspend fun deleteCartProduct(documentId: String)
    suspend fun deleteFavoritesProduct(documentId: String)
    suspend fun signOut()

    suspend fun increaseQuantity(documentId: String, onResult: (Exception?) -> Unit)
    suspend fun decreaseQuantity(documentId: String, onResult: (Exception?) -> Unit)
    fun saveUserInformation(userUid: String, user: User)

}