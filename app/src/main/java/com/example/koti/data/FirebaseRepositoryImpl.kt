package com.example.koti.data

import androidx.lifecycle.viewModelScope
import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.User
import com.example.koti.ui.util.Constants.SUCCESS
import com.example.koti.ui.util.Constants.UNSPECIFIED
import com.example.koti.ui.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Objects

import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val store: FirebaseFirestore
) : FirebaseRepository {
    override suspend fun signUpWithEmailPassword(email: String, password: String): FirebaseUser? {
        auth.createUserWithEmailAndPassword(email, password)
        return auth.currentUser
    }

    override fun signInWithEmailPassword(email: String, password: String): FirebaseUser? {
        auth.signInWithEmailAndPassword(email, password)
        return auth.currentUser
    }

    override fun signOut() {
        auth.signOut()
    }

    override suspend fun getCollection(collectionName: String): Any? {
        val documentReference = store.collection(collectionName).document(auth.uid!!)
        val snapshot = documentReference.get().await()
        return if (snapshot.exists()) {
            val user = snapshot.toObject(User::class.java)
            user
        } else {
            null
        }
    }

    override suspend fun sendPasswordReset(email: String): String {
        var state = UNSPECIFIED
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                state = SUCCESS
            }.addOnFailureListener {
                state = it.message.toString()
            }
        return state
    }
}