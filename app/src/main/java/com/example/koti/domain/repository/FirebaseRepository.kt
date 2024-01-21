package com.example.koti.domain.repository

import com.example.koti.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference

interface FirebaseRepository {

    suspend fun signUpWithEmailPassword(email: String, password: String): FirebaseUser?

    fun signInWithEmailPassword(email: String, password: String): FirebaseUser?

    fun signOut()

    suspend fun getCollection(collectionName: String): Any?

    suspend fun sendPasswordReset(email: String): String

}