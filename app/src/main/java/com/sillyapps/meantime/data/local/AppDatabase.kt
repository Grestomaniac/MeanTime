package com.sillyapps.meantime.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.Template

@Database(entities = [Template::class, Scheme::class], version = 1, exportSchema = false)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract val templatesDao: TemplateDao

    abstract val schemesDao: SchemeDao

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
                        "forecast_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}