package com.bitdrive.screenmonitor.db.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = AppInfo::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("appInfoId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class AppUsage(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val appInfoId: Int
)
