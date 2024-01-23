package com.example.koti.domain.repository

import com.example.koti.model.Address
import com.example.koti.model.CartProduct
import com.example.koti.model.Order
import com.example.koti.model.QuantityChanging
import com.example.koti.model.User
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
    suspend fun getCollection(collectionName: String): Any
    suspend fun getUserInformation(): Any
    suspend fun addNewAddress(address: Address): String
    suspend fun getOrders(): Any
    suspend fun getUserAddresses(): Any
    suspend fun getUserCartProducts(onResult: (List<DocumentSnapshot>?, List<CartProduct>?, Exception?) -> Unit)
    suspend fun placeOrder(order: Order): String


    suspend fun increaseQuantity(documentId: String, onResult: (Exception?) -> Unit)
    suspend fun decreaseQuantity(documentId: String, onResult: (Exception?) -> Unit)
    fun saveUserInformation(userUid: String, user: User)
    fun signOut()
}