package com.example.koti.data.firebase


import android.util.Log
import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.model.Address
import com.example.koti.model.CartProduct
import com.example.koti.model.Order
import com.example.koti.model.Product
import com.example.koti.model.User
import com.example.koti.ui.util.Constants.ADDRESS_COLLECTION
import com.example.koti.ui.util.Constants.CART_COLLECTION
import com.example.koti.ui.util.Constants.FAVORITES_COLLECTION
import com.example.koti.ui.util.Constants.ORDERS_COLLECTION
import com.example.koti.ui.util.Constants.SHOP_PRODUCTS_COLLECTION
import com.example.koti.ui.util.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
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
                    Log.e(TAG, it.message.toString())
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
                    Log.e(TAG, it.message.toString())
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

    override suspend fun getUserInformation(onResult: (User?, Exception?) -> Unit) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        onResult(null, error)
                    } else {
                        val userObj = value?.toObject(User::class.java)
                        userObj?.let {
                            onResult(userObj, null)
                        }
                    }
                }
        }
    }


    override suspend fun sendPasswordReset(email: String, onResult: (Exception?) -> Unit) {
        withContext(Dispatchers.IO) {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    onResult(null)
                }.addOnFailureListener {
                    onResult(it)
                    Log.e(TAG, it.message.toString())
                }
        }
    }

    override suspend fun signOut() {
        withContext(Dispatchers.IO) {
            auth.signOut()
        }
    }

    override suspend fun addNewAddress(address: Address, onResult: (Exception?) -> Unit) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(ADDRESS_COLLECTION)
                .document()
                .set(address)
                .addOnSuccessListener {
                    onResult(null)
                }
                .addOnFailureListener {
                    onResult(it)
                    Log.e(TAG, it.message.toString())
                }
        }
    }

    override suspend fun addNewProductToCart(
        cartProduct: CartProduct,
        onResult: (Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION)
                .document().set(cartProduct)
                .addOnSuccessListener {
                    onResult(null)
                }.addOnFailureListener {
                    onResult(it)
                    Log.e(TAG, it.message.toString())
                }
        }
    }

    override suspend fun placeOrder(order: Order, onResult: (Exception?) -> Unit) {
        withContext(Dispatchers.IO) {
            store.runBatch { _ ->
                store.collection(USER_COLLECTION)
                    .document(auth.uid!!)
                    .collection(ORDERS_COLLECTION)
                    .document()
                    .set(order)

                store.collection(ORDERS_COLLECTION).document().set(order)

                store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION)
                    .get()
                    .addOnSuccessListener { it ->
                        it.documents.forEach {
                            it.reference.delete()
                        }
                    }
            }.addOnSuccessListener {
                onResult(null)
            }.addOnFailureListener {
                onResult(it)
                Log.e(TAG, it.message.toString())
            }
        }
    }

    override suspend fun getOrders(onResult: (List<Order>?, Exception?) -> Unit) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(ORDERS_COLLECTION)
                .get()
                .addOnSuccessListener {
                    val orders = it.toObjects(Order::class.java)
                    onResult(orders, null)
                }.addOnFailureListener {
                    onResult(null, it)
                    Log.e(TAG, it.message.toString())
                }
        }
    }

    override suspend fun addProductToFavorites(
        favProduct: Product,
        onResult: (Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(FAVORITES_COLLECTION)
                .document().set(favProduct)
                .addOnSuccessListener {
                    onResult(null)
                }.addOnFailureListener {
                    onResult(it)
                    Log.e(TAG, it.message.toString())
                }
        }
    }

    override suspend fun getUserAddresses(onResult: (List<DocumentSnapshot>?, List<Address>?, Exception?) -> Unit) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(ADDRESS_COLLECTION)
                .addSnapshotListener { value, error ->
                    if (error != null || value == null) {
                        onResult(null, null, error)
                        Log.e(TAG, error!!.message.toString())
                    } else {
                        val addressesDocuments = value.documents
                        val addresses = value.toObjects(Address::class.java)
                        onResult(addressesDocuments, addresses, null)
                    }
                }
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
                        Log.e(TAG, error!!.message.toString())
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
                Log.e(TAG, it.message.toString())
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
                Log.e(TAG, it.message.toString())
            }
        }
    }

    override suspend fun deleteCartProduct(documentId: String) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(CART_COLLECTION)
                .document(documentId).delete()
                .addOnFailureListener {
                    Log.e(TAG, it.message.toString())
                }
        }
    }

    override suspend fun getUserFavorites(
        onResult: (List<DocumentSnapshot>?, List<Product>?, Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(FAVORITES_COLLECTION)
                .addSnapshotListener { value, error ->
                    if (error != null || value == null) {
                        onResult(null, null, error)
                        Log.e(TAG, error!!.message.toString())
                    } else {
                        val favProductDocuments = value.documents
                        val favProducts = value.toObjects(Product::class.java)
                        onResult(favProductDocuments, favProducts, null)
                    }
                }
        }
    }

    override suspend fun deleteFavoritesProduct(documentId: String) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(FAVORITES_COLLECTION)
                .document(documentId).delete()
                .addOnFailureListener {
                    Log.e(TAG, it.message.toString())
                }
        }
    }

    override suspend fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }


    override suspend fun deleteUserAddress(documentId: String) {
        withContext(Dispatchers.IO) {
            store.collection(USER_COLLECTION).document(auth.uid!!).collection(ADDRESS_COLLECTION)
                .document(documentId).delete()
                .addOnFailureListener {
                    Log.e(TAG, it.message.toString())
                }
        }
    }

    override suspend fun getAllProducts(limit: Long,onResult: (List<Product>?, Exception?) -> Unit) {
        withContext(Dispatchers.IO) {
            store.collection(SHOP_PRODUCTS_COLLECTION)
                .limit(limit).get()
                .addOnSuccessListener { result ->
                    val products = result.toObjects(Product::class.java)
                    onResult(products, null)

                }.addOnFailureListener {
                    onResult(null, it)
                    Log.e("Room", it.message.toString())
                }
        }
    }

}