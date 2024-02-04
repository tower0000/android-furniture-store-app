package com.example.koti.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "products_table")
@Parcelize
data class Product(

    @PrimaryKey(autoGenerate = false)
    val id: String = "",

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "category")
    val category: String = "",

    @ColumnInfo(name = "price")
    val price: Float = 0f,

    @ColumnInfo(name = "offer_percentage")
    val offerPercentage: Float? = null,

    @ColumnInfo(name = "description")
    val description: String? = "",

    @ColumnInfo(name = "colors")
    val colors: List<Int>? = null,

    @ColumnInfo(name = "sizes")
    val sizes: List<String>? = null,

    @ColumnInfo(name = "images")
    val images: List<String> = emptyList()
): Parcelable {
    constructor(): this("0", "", "", 0f, images = emptyList())
}
