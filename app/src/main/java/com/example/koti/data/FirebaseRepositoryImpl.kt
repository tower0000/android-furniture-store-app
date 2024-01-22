package com.example.koti.data


import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Address
import com.example.koti.model.CartProduct
import com.example.koti.model.Order
import com.example.koti.model.User
import com.example.koti.ui.util.Constants.ADDRESS_COLLECTION
import com.example.koti.ui.util.Constants.CART_COLLECTION
import com.example.koti.ui.util.Constants.ORDERS_COLLECTION
import com.example.koti.ui.util.Constants.SUCCESS
import com.example.koti.ui.util.Constants.USER_COLLECTION
import com.example.koti.ui.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
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

    override suspend fun getUserInformation(): Any {
        val userCollectionName = USER_COLLECTION
        return getCollection(userCollectionName)
    }

    override suspend fun getCollection(collectionName: String): Any {
        return try {
            val documentReference = store.collection(collectionName).document(auth.uid!!)
            val snapshot = documentReference.get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                user!!
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

    override suspend fun addNewAddress(address: Address): String {
        return try {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(ADDRESS_COLLECTION).document().set(address)
            SUCCESS
        } catch (e: Exception) {
            val addNewAddressExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            addNewAddressExceptionMessage
        }
    }

    override suspend fun getOrders(): Any {
        return try {
            val query = store.collection(USER_COLLECTION).document(auth.uid!!).collection(
                ORDERS_COLLECTION).get().await()
            val orders = query.toObjects(Order::class.java)
            orders
        } catch (e: Exception) {
            val getOrdersExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            getOrdersExceptionMessage
        }
    }

    override suspend fun placeOrder(order: Order): String {
        return try {
            store.runBatch { batch ->
                store.collection(USER_COLLECTION)
                    .document(auth.uid!!)
                    .collection(ORDERS_COLLECTION)
                    .document()
                    .set(order)

                store.collection(ORDERS_COLLECTION).document().set(order)

                store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION).get()
                    .addOnSuccessListener {
                        it.documents.forEach {
                            it.reference.delete()
                        }
                    }
            }
            SUCCESS
        } catch (e: Exception) {
            val placeOrderExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            placeOrderExceptionMessage
        }
    }

    override suspend fun getUserAddresses(): Any {
        return try {
            val query = store.collection(USER_COLLECTION).document(auth.uid!!).collection(ADDRESS_COLLECTION).get().await()
            val addresses = query?.toObjects(Address::class.java)
            addresses!!
        } catch (e: Exception) {
            val getUserAddressesExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            getUserAddressesExceptionMessage
        }
    }

    override suspend fun getUserCartProducts(): Any {
        return try {
            val query = store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION).get().await()
            val cartProducts = query?.toObjects(CartProduct::class.java)
            cartProducts!!
        } catch (e: Exception) {
            val getUserCartProductsExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            getUserCartProductsExceptionMessage
        }
    }
}