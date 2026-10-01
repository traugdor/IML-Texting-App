package com.hack.smsgroups.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hack.smsgroups.data.model.Label
import com.hack.smsgroups.data.model.LabelAddress

@Database(
    entities = [Label::class, LabelAddress::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labelDao(): LabelDao
    abstract fun labelAddressDao(): LabelAddressDao

    companion object {
        const val NAME = "sms_groups.db"
    }
}
