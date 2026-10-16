package com.example.bugs_android.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "scores",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,
    val points: Int,
    val difficulty: Int,
    val hits: Int,
    val misses: Int,
    val accuracy: Float,
    val date: Long
)
