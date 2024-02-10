package com.chimzeart.yangachecker.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

import androidx.room.Query

@Dao
interface YangaBundleDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(yangaBundle: YangaBundle): Long

    @Query("SELECT * FROM yanga_bundle_table")
    fun getAllLiveData(): LiveData<List<YangaBundle>>

//    @Query("SELECT * FROM yanga_bundle_table ORDER BY innovationId DESC")
    @Query("SELECT * FROM yanga_bundle_table")
    suspend fun getAll(): List<YangaBundle>

}
