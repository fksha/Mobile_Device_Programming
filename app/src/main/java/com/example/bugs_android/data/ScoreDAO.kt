package com.example.bugs_android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScoreDAO {
    @Insert
    suspend fun insertScore(score: ScoreEntity)

    @Query("""
        SELECT scores.id,
               scores.userId,
               users.name AS userName,
               scores.points,
               scores.difficulty,
               scores.hits,
               scores.misses,
               scores.accuracy,
               scores.date
        FROM scores
        INNER JOIN users ON scores.userId = users.id
        ORDER BY scores.points DESC
    """)
    suspend fun getAllScores(): List<ScoreWithUsers>
}