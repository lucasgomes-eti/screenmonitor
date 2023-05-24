package com.bitdrive.screenmonitor.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bitdrive.screenmonitor.db.model.AppInfo
import com.bitdrive.screenmonitor.db.model.AppUsage

@Database(
    entities = [AppInfo::class, AppUsage::class],
    version = 1,
    exportSchema = false
)
abstract class Database : RoomDatabase() {
}