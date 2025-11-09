package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Profile::class, Favorite::class, Memory::class, Event::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun memoryDao(): MemoryDao
    abstract fun eventDao(): EventDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                DatabaseBackupManager.restoreIfNeeded(context)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lover_app_database"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            DatabaseBackupManager.backupDatabase(context)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE events ADD COLUMN endDate TEXT NOT NULL DEFAULT ''")
        database.execSQL("UPDATE events SET endDate = date WHERE endDate = ''")
    }
}

private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE events ADD COLUMN color TEXT NOT NULL DEFAULT ''")
        database.execSQL("UPDATE events SET color = '#FFE0E0' WHERE isAnniversary = 1 AND (color = '' OR color IS NULL)")
    }
}



