package com.arkadiusz.dayscounter.di

import android.content.Context
import android.content.res.Resources
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideDatabaseRepository(): DatabaseRepository {
        return DatabaseRepository()
    }

    @Provides
    fun provideResources(@ApplicationContext context: Context): Resources = context.resources
}