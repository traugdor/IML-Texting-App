package com.hack.smsgroups.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hack.smsgroups.data.model.LabelAddress
import kotlinx.coroutines.flow.Flow

data class AddressLabelRow(
    val addressNorm: String,
    val name: String
)

@Dao
interface LabelAddressDao {

    @Query("SELECT * FROM label_addresses WHERE labelId = :labelId ORDER BY addressNorm ASC")
    fun observeForLabel(labelId: Long): Flow<List<LabelAddress>>

    @Query("SELECT * FROM label_addresses WHERE labelId = :labelId ORDER BY addressNorm ASC")
    suspend fun forLabelOnce(labelId: Long): List<LabelAddress>

    @Query("SELECT addressNorm, name FROM label_addresses INNER JOIN labels ON labels.id = label_addresses.labelId")
    suspend fun allWithNames(): List<AddressLabelRow>

    @Query("SELECT * FROM label_addresses WHERE addressNorm = :norm LIMIT 1")
    suspend fun byNormalized(norm: String): LabelAddress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(address: LabelAddress): Long

    @Query("DELETE FROM label_addresses WHERE labelId = :labelId AND addressNorm = :norm")
    suspend fun delete(labelId: Long, norm: String)
}
