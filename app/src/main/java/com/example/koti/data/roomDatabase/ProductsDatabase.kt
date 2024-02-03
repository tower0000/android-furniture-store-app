package com.example.koti.data.roomDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.koti.domain.model.Product
import com.example.koti.ui.util.Converters

@Database (entities = [Product::class], version = 1)
@TypeConverters(Converters::class)
abstract class ProductsDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}