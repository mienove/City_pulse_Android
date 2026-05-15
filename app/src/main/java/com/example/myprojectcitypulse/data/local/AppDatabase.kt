package com.example.myprojectcitypulse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.JournalMode

@Database(entities = [com.example.myprojectcitypulse.model.Lieux::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lieuxDAO(): LieuxDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "citypulse_database"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // Fixes database concurrency locks
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
