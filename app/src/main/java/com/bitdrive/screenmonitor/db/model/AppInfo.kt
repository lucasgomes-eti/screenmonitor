package com.bitdrive.screenmonitor.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null
)