package com.chimzeart.yangachecker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "yanga_bundle_table")

data class YangaBundle (
    @PrimaryKey
    val bundleCacheId: String,
    val cacheExpiry: String,
    val validity:Int,
    val title:String,
    val name:String,
    val price: Int,
    val size: Int,
    val shouldAutoBuy: Boolean = false,
    val dateRecorded: Long = Date().time
        )
