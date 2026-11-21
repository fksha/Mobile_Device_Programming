package com.example.bugs_android.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao //объект доступа к данным
interface UserDAO {
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users ORDER BY name ASC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): UserEntity?

    @Delete
    suspend fun deleteUser(user: UserEntity)
}