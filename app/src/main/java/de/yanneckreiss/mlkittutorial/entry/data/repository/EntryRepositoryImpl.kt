package de.yanneckreiss.mlkittutorial.entry.data.repository

import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.EntryDao
import de.yanneckreiss.mlkittutorial.entry.domain.repository.IEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(
    private val entryDao: EntryDao
) : IEntryRepository {

    override fun getAllEntries(): Flow<List<Entry>> {
        return entryDao.getAll()
    }

    override suspend fun insertEntry(newEntry: Entry): Flow<List<Entry>> {
        entryDao.insertAll(newEntry)
        return getAllEntries()
    }

    override suspend fun insertList(entries: List<Entry>) {
        entryDao.insertList(entries)
    }

    override suspend fun deleteEntry(entry: Entry): Flow<List<Entry>> {
        entryDao.delete(entry)
        return getAllEntries()
    }

    override suspend fun deleteById(id: Int) {
        entryDao.deleteById(id)
    }

    override suspend fun deleteAllEntries() {
        entryDao.deleteAll()
    }

    override suspend fun updateEntry(entry: Entry) {
        entryDao.updateEntry(entry)
    }

    /**
     * Mock data seeding has been removed to ensure only actual user-recorded data is stored.
     */
    override suspend fun seedSampleData() {
        // No mock data insertion - user recorded entries only
    }
}
