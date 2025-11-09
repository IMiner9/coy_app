package com.example.myapplication.data

import android.content.Context
import java.io.File

object DatabaseBackupManager {
    private const val DATABASE_NAME = "lover_app_database"
    private const val BACKUP_DIR_NAME = "database_backup"
    private const val BACKUP_FILE_NAME = "lover_app_database_backup.db"

    fun restoreIfNeeded(context: Context) {
        runCatching {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (dbFile.exists()) return
            val backupFile = File(context.filesDir, "$BACKUP_DIR_NAME/$BACKUP_FILE_NAME")
            if (backupFile.exists()) {
                backupFile.parentFile?.mkdirs()
                dbFile.parentFile?.mkdirs()
                backupFile.copyTo(dbFile, overwrite = true)
            }
        }
    }

    fun backupDatabase(context: Context) {
        runCatching {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) return
            val backupDir = File(context.filesDir, BACKUP_DIR_NAME)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            val backupFile = File(backupDir, BACKUP_FILE_NAME)
            dbFile.copyTo(backupFile, overwrite = true)
        }
    }
}

