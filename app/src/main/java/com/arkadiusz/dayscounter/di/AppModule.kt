package com.arkadiusz.dayscounter.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.arkadiusz.dayscounter.data.remote.UnsplashService
import com.arkadiusz.dayscounter.util.PreferenceUtils
import com.arkadiusz.dayscounter.util.PurchasesUtils
import com.arkadiusz.dayscounter.util.billing.BillingDataSource
import com.arkadiusz.dayscounter.util.billing.BillingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceUtils.defaultPrefs(context)
    }

    @Provides
    fun provideResources(@ApplicationContext context: Context): Resources {
        return context.resources
    }

    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideUnsplashService(): UnsplashService {
        return UnsplashService.getService()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Singleton
    @Provides
    fun provideBillingDataSource(@ApplicationContext context: Context): BillingDataSource {
        return BillingDataSource.getInstance(
            context as Application,
            GlobalScope,
            arrayOf(PurchasesUtils.PREMIUM_SKU, PurchasesUtils.PREMIUM_BIG_SKU),
            null,
            null
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Singleton
    @Provides
    fun provideBillingRepository(
        billingDataSource: BillingDataSource,
        sharedPreferences: SharedPreferences,
    ): BillingRepository {
        return BillingRepository(
            GlobalScope,
            billingDataSource,
            sharedPreferences
        )
    }
}