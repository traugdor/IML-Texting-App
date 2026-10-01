package com.hack.smsgroups.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hack.smsgroups.data.model.Label
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {

    @Query("SELECT * FROM labels ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<Label>>

    @Query("SELECT * FROM labels ORDER BY sortOrder ASC, name ASC")
    suspend fun allOnce(): List<Label>

    @Query("SELECT * FROM labels WHERE id = :id")
    suspend fun byId(id: Long): Label?

    @Insert
    suspend fun insert(label: Label): Long

    @Update
    suspend fun update(label: Label)

    @Delete
    suspend fun delete(label: Label)
}
