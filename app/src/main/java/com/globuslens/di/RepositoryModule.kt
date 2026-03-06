package com.globuslens.di

import com.globuslens.repository.ProductRepository
import com.globuslens.repository.ShoppingListRepository
import com.globuslens.repository.TranslationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @ViewModelScoped
    fun provideProductRepository(
        repository: ProductRepository
    ): ProductRepository = repository

    @Provides
    @ViewModelScoped
    fun provideShoppingListRepository(
        repository: ShoppingListRepository
    ): ShoppingListRepository = repository

    @Provides
    @ViewModelScoped
    fun provideTranslationRepository(
        repository: TranslationRepository
    ): TranslationRepository = repository
}