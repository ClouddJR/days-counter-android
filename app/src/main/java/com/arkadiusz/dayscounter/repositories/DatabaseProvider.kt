package com.arkadiusz.dayscounter.repositories

class DatabaseProvider {

    companion object {
        private var repository: DatabaseRepository? = null

        fun provideRepository(): DatabaseRepository {
            if (repository == null) {
                repository = DatabaseRepository()
            }

            return repository!!
        }
    }
}