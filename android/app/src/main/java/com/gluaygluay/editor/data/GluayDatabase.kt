package com.gluaygluay.editor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProjectEntity::class],
    version = 1,
    exportSchema = true
)
abstract class GluayDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile private var INSTANCE: GluayDatabase? = null

        fun getInstance(context: Context): GluayDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GluayDatabase::class.java,
                    "gluay_gluay_projects.db"
                ).build().also { INSTANCE = it }
            }
    }
}
