package com.arkadiusz.dayscounter.data.local

class DatabaseProvider {

    companion object {
        private val repository: DatabaseRepository by lazy { DatabaseRepository() }

        fun provideRepository(): DatabaseRepository {
            return repository
        }
    }
}