package de.yanneckreiss.mlkittutorial.entry.data.repository

import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.data.local.EntryDao
import de.yanneckreiss.mlkittutorial.entry.domain.repository.IEntryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

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

    override suspend fun deleteEntry(entry: Entry): Flow<List<Entry>> {
        entryDao.delete(entry)
        return getAllEntries()
    }

    override suspend fun updateEntry(entry: Entry) {
        val existingEntryList = entryDao.loadAllByIds(entry.id)
        val existingEntry = existingEntryList.firstOrNull() ?: return

        val updated = existingEntry.copy(
            id = entry.id,
            description = entry.description,
            meterReading = entry.meterReading
        )
        entryDao.updateEntry(updated)
    }
}
