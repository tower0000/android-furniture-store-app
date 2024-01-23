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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val store: FirebaseFirestore
) : FirebaseRepository {

    private val TAG = this.javaClass.simpleName

    override suspend fun signUpWithEmailPassword(
        user: User,
        password: String,
        onResult: (Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            auth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener { it ->
                    it.user?.let {
                        saveUserInformation(it.uid, user)
                    }
                    onResult(null)
                }.addOnFailureListener {
                    onResult(it)
                }
        }
    }

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String,
        onResult: (Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    onResult(null)
                }.addOnFailureListener {
                    onResult(it)
                }
        }
    }

    override fun saveUserInformation(userUid: String, user: User) {
        try {
            store.collection(USER_COLLECTION).document(userUid).set(user)
        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
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

    override suspend fun sendPasswordReset(email: String, onResult: (Exception?) -> Unit) {
        withContext(Dispatchers.IO) {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    onResult(null)
                }.addOnFailureListener {
                    onResult(it)
                }
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override suspend fun addNewAddress(address: Address): String {
        return try {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(ADDRESS_COLLECTION)
                .document().set(address)
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
                ORDERS_COLLECTION
            ).get().await()
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

                store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION)
                    .get()
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
            val query = store.collection(USER_COLLECTION).document(auth.uid!!)
                .collection(ADDRESS_COLLECTION).get().await()
            val addresses = query?.toObjects(Address::class.java)
            addresses!!
        } catch (e: Exception) {
            val getUserAddressesExceptionMessage = "${e.message}"
            e.message?.let { Log.e(TAG, it) }
            getUserAddressesExceptionMessage
        }
    }

    override suspend fun getUserCartProducts(
        onResult: (List<DocumentSnapshot>?, List<CartProduct>?, Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION)
                .addSnapshotListener { value, error ->
                    if (error != null || value == null) {
                        onResult(null, null, error)
                    } else {
                        val cartProductDocuments = value.documents
                        val cartProducts = value.toObjects(CartProduct::class.java)
                        onResult(cartProductDocuments, cartProducts, null)
                    }
                }
        }
    }

    override suspend fun increaseQuantity(
        documentId: String,
        onResult: (Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            store.runTransaction { transition ->
                val documentRef =
                    store.collection(USER_COLLECTION).document(auth.uid!!).collection(
                        CART_COLLECTION
                    ).document(documentId)
                val document = transition.get(documentRef)
                val productObject = document.toObject(CartProduct::class.java)
                productObject?.let { cartProduct ->
                    val newQuantity = cartProduct.quantity + 1
                    val newProductObject = cartProduct.copy(quantity = newQuantity)
                    transition.set(documentRef, newProductObject)
                }
            }.addOnSuccessListener {
                onResult(null)
            }.addOnFailureListener {
                onResult(it)
            }
        }
    }

    override suspend fun decreaseQuantity(
        documentId: String,
        onResult: (Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            store.runTransaction { transition ->
                val documentRef = store.collection(USER_COLLECTION).document(auth.uid!!).collection(
                    CART_COLLECTION
                ).document(documentId)
                val document = transition.get(documentRef)
                val productObject = document.toObject(CartProduct::class.java)
                productObject?.let { cartProduct ->
                    val newQuantity = cartProduct.quantity - 1
                    val newProductObject = cartProduct.copy(quantity = newQuantity)
                    transition.set(documentRef, newProductObject)
                }
            }.addOnSuccessListener {
                onResult(null)
            }.addOnFailureListener {
                onResult(it)
            }
        }
    }

}