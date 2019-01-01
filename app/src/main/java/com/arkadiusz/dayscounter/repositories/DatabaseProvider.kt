package com.arkadiusz.dayscounter.repositories

class DatabaseProvider {

    companion object {
        private val repository: DatabaseRepository by lazy { DatabaseRepository() }

        fun provideRepository(): DatabaseRepository {
            return repository
        }
    }
}