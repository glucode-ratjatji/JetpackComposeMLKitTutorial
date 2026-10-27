package de.yanneckreiss.mlkittutorial.entry.domain.repository

import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import kotlinx.coroutines.flow.Flow

interface IEntryRepository {
    fun getAllEntries(): Flow<List<Entry>>
    suspend fun insertEntry(newEntry: Entry): Flow<List<Entry>>
    suspend fun deleteEntry(entry: Entry): Flow<List<Entry>>
    suspend fun updateEntry(entry: Entry)
}
