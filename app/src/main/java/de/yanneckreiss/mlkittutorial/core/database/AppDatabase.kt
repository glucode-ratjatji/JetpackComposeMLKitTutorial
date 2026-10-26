package de.yanneckreiss.mlkittutorial.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.EntryDao

@Database(entities = [Entry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
}
