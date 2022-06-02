package com.chimzeart.yangachecker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [YangaBundle::class], version = 2, exportSchema = false)
abstract class YangaDatabase: RoomDatabase() {

    abstract val yangaBundleDao: YangaBundleDao

    companion object{

        @Volatile
        private var INSTANCE : YangaDatabase? = null

        fun getInstance(context: Context): YangaDatabase {
            synchronized(this){

                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        YangaDatabase::class.java,
                        "yanga_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}