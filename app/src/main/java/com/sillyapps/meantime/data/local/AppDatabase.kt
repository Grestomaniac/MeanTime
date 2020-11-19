package com.sillyapps.meantime.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sillyapps.meantime.data.ApplicationPreferences
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.Template
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Database(entities = [Template::class, Scheme::class, ApplicationPreferences::class], version = 5, exportSchema = false)
@TypeConverters(AppTypeConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract val templatesDao: TemplateDao

    abstract val schemesDao: SchemeDao

    abstract val appPrefDao: ApplicationPreferencesDao

    abstract val taskGoalsDao: TaskGoalsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database")
                        .addCallback(DatabaseCallback())
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }

    private class DatabaseCallback(): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { appDatabase ->
                CoroutineScope(Dispatchers.IO).launch {
                    appDatabase.appPrefDao.insert(ApplicationPreferences())
                    appDatabase.schemesDao.insert(Scheme(1))
                }
            }
        }

    }
}