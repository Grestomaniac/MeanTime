package com.sillyapps.meantime.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sillyapps.meantime.data.ApplicationPreferences
import com.sillyapps.meantime.data.Scheme
import com.sillyapps.meantime.data.TaskGoals
import com.sillyapps.meantime.data.Template
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Database(entities = [Template::class, Scheme::class, ApplicationPreferences::class, TaskGoals::class], version = 7)
@TypeConverters(AppTypeConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract val templatesDao: TemplateDao

    abstract val schemesDao: SchemeDao

    abstract val appPrefDao: ApplicationPreferencesDao

    abstract val taskGoalsDao: TaskGoalsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("create table goal_table (id integer primary key not null, name text not null, goals text not null)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val convertedEmptyList = AppTypeConverter.convertGoalsListToJson(mutableListOf())
                database.execSQL("alter table goal_table rename column goals to activeGoals")
                database.execSQL("alter table goal_table add column completedGoals text not null default $convertedEmptyList")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database")
                        .addCallback(DatabaseCallback())
                        .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
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