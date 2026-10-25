package de.yanneckreiss.mlkittutorial.entry.data.TaskViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.yanneckreiss.mlkittutorial.entry.data.local.Entry
import de.yanneckreiss.mlkittutorial.entry.domain.repository.IEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: IEntryRepository
) : ViewModel() {

    val visibleEntrys: StateFlow<List<Entry>> = repository.getAllEntrys().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addEntry(description: String) {
        viewModelScope.launch {
            val newEntry = Entry(
                description = description,
                timestamp = System.currentTimeMillis().toString()
            )
            repository.insertEntry(newEntry)
        }
    }

    fun deleteEntry(entry: Entry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun onEntryUpdated(updatedEntry: Entry) {
        viewModelScope.launch {
            repository.updateEntry(updatedEntry)
        }
    }
}
