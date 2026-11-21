package com.example.bugs_android.data
//для хранения результата SQL-запроса.
data class ScoreWithUsers(
    val id: Int,
    val userId: Int,
    val userName: String,
    val points: Int,
    val difficulty: Int,
    val speed: Int,
    val roundDuration: Int,
    val hits: Int,
    val misses: Int,
    val accuracy: Float,
    val date: Long
)
