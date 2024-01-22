package com.example.koti.domain.repository

import com.example.koti.model.Address
import com.example.koti.model.Order
import com.example.koti.model.User

interface FirebaseRepository {
    suspend fun signUpWithEmailPassword(user: User, password: String): String
    suspend fun signInWithEmailPassword(email: String, password: String): String
    suspend fun sendPasswordReset(email: String): String
    suspend fun getCollection(collectionName: String): Any
    suspend fun getUserInformation(): Any
    suspend fun addNewAddress(address: Address): String
    suspend fun getOrders(): Any
    suspend fun getUserAddresses(): Any
    suspend fun getUserCartProducts(): Any
    suspend fun placeOrder(order: Order): String
    fun saveUserInformation(userUid: String, user: User): String
    fun signOut()
}