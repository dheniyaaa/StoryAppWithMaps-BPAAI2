package com.example.submission1intermediate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey
    val id: String,
    val prevkey: Int?,
    val nextKey: Int?
)