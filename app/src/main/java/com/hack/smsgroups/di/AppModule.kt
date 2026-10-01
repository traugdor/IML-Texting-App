package com.hack.smsgroups.di

import android.content.Context
import androidx.room.Room
import com.hack.smsgroups.data.db.AppDatabase
import com.hack.smsgroups.data.db.LabelAddressDao
import com.hack.smsgroups.data.db.LabelDao
import com.hack.smsgroups.domain.LabelLookup
import com.hack.smsgroups.domain.LabelResolver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLabelDao(db: AppDatabase): LabelDao = db.labelDao()

    @Provides
    fun provideLabelAddressDao(db: AppDatabase): LabelAddressDao = db.labelAddressDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ResolverModule {

    @Binds
    abstract fun bindLabelLookup(impl: LabelResolver): LabelLookup
}
