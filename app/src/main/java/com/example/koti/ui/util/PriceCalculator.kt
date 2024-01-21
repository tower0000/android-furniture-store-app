package com.example.koti.ui.util

fun Float?.getProductPrice(price: Float): Float {
    //this --> Percentage
    if (this == null)
        return price
    val remainingPricePercentage = 1f - this
    val priceAfterOffer = remainingPricePercentage * price

    return priceAfterOffer
}