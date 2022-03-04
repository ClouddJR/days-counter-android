package com.arkadiusz.dayscounter.di

import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideDatabaseRepository(): DatabaseRepository {
        return DatabaseRepository()
    }
}