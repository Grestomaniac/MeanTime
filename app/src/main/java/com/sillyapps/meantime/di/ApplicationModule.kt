package com.sillyapps.meantime.di

import android.content.Context
import com.sillyapps.meantime.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
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
    fun provideAppPrefDao(db: AppDatabase) = db.appPrefDao

    @Singleton
    @Provides
    fun provideTaskGoalsDao(db: AppDatabase) = db.taskGoalsDao

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO
}