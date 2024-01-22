package com.example.koti.data


import android.util.Log
import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.User
import com.example.koti.ui.util.Constants.SUCCESS
import com.example.koti.ui.util.Constants.USER_COLLECTION
import com.example.koti.ui.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val store: FirebaseFirestore
) : FirebaseRepository {

    private val TAG = this.javaClass.simpleName
    override suspend fun signUpWithEmailPassword(user: User, password: String): String {
        return try {
            auth.createUserWithEmailAndPassword(user.email, password).addOnSuccessListener { it ->
                it.user?.let {
                    saveUserInformation(it.uid, user)
                }
            }
            SUCCESS
        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
            val signUpExceptionMessage = "${e.message}"
            signUpExceptionMessage
        }
    }

    override suspend fun signInWithEmailPassword(email: String, password: String): String {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            SUCCESS
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    val signInWrongPassExceptionMessage = "Wrong password"
                    signInWrongPassExceptionMessage
                }

                else -> {
                    e.message?.let { Log.e(TAG, it) }
                    val signInExceptionMessage = "${e.message}"
                    signInExceptionMessage
                }
            }
        }
    }

    override fun saveUserInformation(userUid: String, user: User): String {
        return try {
            store.collection(USER_COLLECTION).document(userUid).set(user)
            SUCCESS
        } catch (e: Exception) {
            val saveUserInformationExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            saveUserInformationExceptionMessage
        }
    }

    override suspend fun getUserInformation(): Any? {
        val userCollectionName = USER_COLLECTION
        return getCollection(userCollectionName)
    }

    override suspend fun getCollection(collectionName: String): Any? {
        return try {
            val documentReference = store.collection(collectionName).document(auth.uid!!)
            val snapshot = documentReference.get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                user
            } else {
                val getCollectionError = "Collection doesn't exist"
                Log.e(TAG, getCollectionError)
                getCollectionError
            }
        } catch (e: Exception) {
            val getCollectionExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            getCollectionExceptionMessage
        }
    }

    override suspend fun sendPasswordReset(email: String): String {
        return try {
            auth.sendPasswordResetEmail(email)
            SUCCESS
        } catch (e: Exception) {
            val sendPasswordResetExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            sendPasswordResetExceptionMessage
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}