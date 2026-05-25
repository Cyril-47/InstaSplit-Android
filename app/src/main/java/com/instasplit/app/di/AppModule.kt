package com.instasplit.app.di

import com.instasplit.app.data.repository.ExportRepositoryImpl
import com.instasplit.app.data.repository.ImageRepositoryImpl
import com.instasplit.app.domain.repository.ExportRepository
import com.instasplit.app.domain.repository.ImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds @Singleton
    abstract fun bindExportRepository(impl: ExportRepositoryImpl): ExportRepository
}

