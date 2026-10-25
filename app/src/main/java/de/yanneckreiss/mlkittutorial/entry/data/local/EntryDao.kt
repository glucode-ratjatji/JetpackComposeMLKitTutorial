package de.yanneckreiss.mlkittutorial.entry.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entry")
    fun getAll(): Flow<List<Entry>>

    @Query("SELECT * FROM entry WHERE id IN (:entryIds)")
    fun loadAllByIds(entryIds: Int): List<Entry>

    @Insert
    suspend fun insertAll(vararg entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Update
    suspend fun updateEntry(entry: Entry)
}
