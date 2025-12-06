package com.vlaados.freeze.di

import com.vlaados.freeze.data.repository.AuthRepository
import com.vlaados.freeze.data.repository.AuthRepositoryImpl
import com.vlaados.freeze.data.repository.UserRepository
import com.vlaados.freeze.data.repository.UserRepositoryImpl
import com.vlaados.freeze.data.repository.SavingsRepository
import com.vlaados.freeze.data.repository.SavingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindSavingsRepository(
        savingsRepositoryImpl: SavingsRepositoryImpl
    ): SavingsRepository
}
