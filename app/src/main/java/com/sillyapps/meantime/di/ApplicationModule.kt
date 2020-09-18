package com.sillyapps.meantime.di

import android.content.Context
import com.sillyapps.meantime.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context) = AppDatabase.getInstance(appContext)

    @Singleton
    @Provides
    fun provideTemplateDao(db: AppDatabase) = db.templatesDao

    @Singleton
    @Provides
    fun provideSchemeDao(db: AppDatabase) = db.schemesDao

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO
}

@Module
@InstallIn(ApplicationComponent::class)
abstract class RepositoryModule {
}