package de.yanneckreiss.mlkittutorial.entry.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entry ORDER BY id DESC")
    fun getAll(): Flow<List<Entry>>

    @Query("SELECT * FROM entry WHERE id IN (:entryIds)")
    fun loadAllByIds(entryIds: Int): List<Entry>

    @Query("SELECT * FROM entry ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentEntries(limit: Int): List<Entry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entry: Entry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(entries: List<Entry>)

    @Delete
    suspend fun delete(entry: Entry)

    @Query("DELETE FROM entry WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM entry")
    suspend fun deleteAll()

    @Update
    suspend fun updateEntry(entry: Entry)
}
