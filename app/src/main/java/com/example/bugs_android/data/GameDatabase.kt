package com.example.bugs_android.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun userDao(): UserDAO

    abstract fun scoreDao(): ScoreDAO
}