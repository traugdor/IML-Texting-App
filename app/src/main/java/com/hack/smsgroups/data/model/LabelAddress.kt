package com.hack.smsgroups.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "label_addresses",
    foreignKeys = [
        ForeignKey(
            entity = Label::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("labelId"), Index(value = ["addressNorm"], unique = true)]
)
data class LabelAddress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val labelId: Long,
    val addressRaw: String,
    val addressNorm: String
)
