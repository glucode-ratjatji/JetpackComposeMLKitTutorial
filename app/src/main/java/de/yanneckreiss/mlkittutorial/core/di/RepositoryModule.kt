package de.yanneckreiss.mlkittutorial.core.di

import de.yanneckreiss.mlkittutorial.entry.data.repository.EntryRepositoryImpl
import de.yanneckreiss.mlkittutorial.entry.domain.repository.IEntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEntryRepository(
        entryRepositoryImpl: EntryRepositoryImpl
    ): IEntryRepository
}
