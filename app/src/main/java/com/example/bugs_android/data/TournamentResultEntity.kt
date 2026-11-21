
package com.example.bugs_android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournament_results")
data class TournamentResultEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val tournamentId: String,
    val tournamentDate: Long,

    val userId: Int,
    val userName: String,

    val score: Int,
    val hits: Int,
    val misses: Int,
    val accuracy: Int
)
