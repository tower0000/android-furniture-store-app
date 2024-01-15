package com.example.koti.data.order

sealed class OrderStatus(val status: String) {

    data object Ordered: OrderStatus("Ordered")
    data object Canceled: OrderStatus("Canceled")
    data object Confirmed: OrderStatus("Confirmed")
    data object Shipped: OrderStatus("Shipped")
    data object Delivered: OrderStatus("Delivered")
    data object Returned: OrderStatus("Returned")

}