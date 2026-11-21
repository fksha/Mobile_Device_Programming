
package com.example.bugs_android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TournamentResultDAO {

    @Insert
    suspend fun insertResults(results: List<TournamentResultEntity>)

    @Query("""
        SELECT * FROM tournament_results
        WHERE tournamentId = :tournamentId
        ORDER BY score DESC, accuracy DESC, userId ASC
    """)
    suspend fun getResultsForTournament(
        tournamentId: String
    ): List<TournamentResultEntity>

    @Query("""
        SELECT
            tournamentId,
            tournamentDate,
            COUNT(*) AS participantCount,
            MAX(score) AS bestScore
        FROM tournament_results
        GROUP BY tournamentId, tournamentDate
        ORDER BY tournamentDate DESC
    """)
    suspend fun getTournamentHistory(): List<TournamentSummary>
}

data class TournamentSummary(
    val tournamentId: String,
    val tournamentDate: Long,
    val participantCount: Int,
    val bestScore: Int
)
