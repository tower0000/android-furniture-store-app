package com.example.koti.di

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.room.Room
import com.example.koti.data.firebase.FirebaseRepositoryImpl
import com.example.koti.data.roomDatabase.ProductDao
import com.example.koti.data.roomDatabase.ProductsDatabase
import com.example.koti.data.roomDatabase.ProductsDatabaseRepositoryImpl
import com.example.koti.domain.repository.FirebaseRepository
import com.example.koti.domain.repository.ProductsDatabaseRepository
import com.example.koti.ui.util.Constants.INTRODUCTION_SP
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestoreDatabase() = Firebase.firestore

    @Provides
    fun provideIntroductionSharedPrefs(
        application: Application
    ) = application.getSharedPreferences(INTRODUCTION_SP, MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideStorage() = FirebaseStorage.getInstance().reference

    @Provides
    fun provideMyRepository(auth: FirebaseAuth, store: FirebaseFirestore): FirebaseRepository {
        return FirebaseRepositoryImpl(auth, store)
    }

    @Provides
    @Singleton
    fun provide(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, ProductsDatabase::class.java, "products_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideDao(db: ProductsDatabase) = db.productDao()

    @Provides
    @Singleton
    fun provideRoomRepository(
        dao: ProductDao,
    ): ProductsDatabaseRepository {
        return ProductsDatabaseRepositoryImpl(dao)
    }
}