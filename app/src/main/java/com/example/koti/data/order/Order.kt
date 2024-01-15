package com.example.koti.data.order

import com.example.koti.data.Address
import com.example.koti.data.CartProduct

data class Order(
    val orderStatus: String,
    val totalPrice: Float,
    val products: List<CartProduct>,
    val address: Address
)
