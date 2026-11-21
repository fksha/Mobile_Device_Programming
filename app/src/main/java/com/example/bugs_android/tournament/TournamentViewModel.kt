
package com.example.bugs_android.tournament

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bugs_android.data.UserEntity
import java.util.UUID

data class TournamentResult(
    val userId: Int,
    val userName: String,
    val score: Int,
    val hits: Int,
    val misses: Int,
    val accuracy: Int
)

class TournamentViewModel : ViewModel() {

    var participants = mutableStateListOf<UserEntity>()
        private set

    var results = mutableStateListOf<TournamentResult>()
        private set

    var currentPlayerIndex = mutableIntStateOf(0)
        private set

    var isTournamentActive = mutableStateOf(false)
        private set

    var isTournamentFinished = mutableStateOf(false)
        private set

    var isRoundStarted = mutableStateOf(false)
        private set

    var tournamentId = mutableStateOf("")
        private set

    var tournamentDate = mutableStateOf(0L)
        private set

    var areResultsSaved = mutableStateOf(false)
        private set

    val currentPlayer: UserEntity?
        get() = participants.getOrNull(currentPlayerIndex.intValue)

    fun startTournament(users: List<UserEntity>) {
        participants.clear()
        participants.addAll(users)

        results.clear()

        currentPlayerIndex.intValue = 0

        tournamentId.value = UUID.randomUUID().toString()
        tournamentDate.value = System.currentTimeMillis()

        areResultsSaved.value = false

        isTournamentActive.value = true
        isTournamentFinished.value = false
        isRoundStarted.value = false
    }

    fun startCurrentRound() {
        if (isTournamentActive.value && !isTournamentFinished.value) {
            isRoundStarted.value = true
        }
    }

    fun finishCurrentRound(
        score: Int,
        hits: Int,
        misses: Int
    ) {
        val player = currentPlayer ?: return

        val accuracy = if (hits + misses > 0) {
            hits * 100 / (hits + misses)
        } else {
            0
        }

        results.add(
            TournamentResult(
                userId = player.id,
                userName = player.name,
                score = score,
                hits = hits,
                misses = misses,
                accuracy = accuracy
            )
        )

        isRoundStarted.value = false

        if (currentPlayerIndex.intValue < participants.lastIndex) {
            currentPlayerIndex.intValue++
        } else {
            isTournamentActive.value = false
            isTournamentFinished.value = true
        }
    }

    fun markResultsSaved() {
        areResultsSaved.value = true
    }

    fun resetTournament() {
        participants.clear()
        results.clear()

        currentPlayerIndex.intValue = 0

        tournamentId.value = ""
        tournamentDate.value = 0L
        areResultsSaved.value = false

        isTournamentActive.value = false
        isTournamentFinished.value = false
        isRoundStarted.value = false
    }
}
