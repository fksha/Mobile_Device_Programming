package com.example.bugs_android

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bugs_android.data.UserEntity

data class TournamentResult(
    val userId: Int,
    val userName: String,
    val score: Int,
    val hits: Int,
    val misses: Int,
    val accuracy: Int
)

class TournamentViewModel : ViewModel() {
    //участники турнира
    var participants = mutableStateListOf<UserEntity>()
        private set

    var results = mutableStateListOf<TournamentResult>()
        private set

    var currentPlayerIndex = mutableIntStateOf(0)
        private set

    //состояние турнира
    var isTournamentActive = mutableStateOf(false)
        private set

    var isTournamentFinished = mutableStateOf(false)
        private set

    var isRoundStarted = mutableStateOf(false)
        private set

    // Текущий участник
    val currentPlayer: UserEntity?
        get() = participants.getOrNull(currentPlayerIndex.intValue)

    // Начало турнира
    fun startTournament(users: List<UserEntity>) {
        participants.clear()
        participants.addAll(users)

        currentPlayerIndex.intValue = 0

        isTournamentActive.value = true
        isTournamentFinished.value = false
        isRoundStarted.value = false
    }

    fun startCurrentRound() {
        if (isTournamentActive.value && !isTournamentFinished.value) {
            isRoundStarted.value = true
        }
    }

    // Завершение раунда текущего участника
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

        // Проверяем, остались ли участники
        if (currentPlayerIndex.intValue < participants.lastIndex) {
            currentPlayerIndex.intValue++
        } else {
            isTournamentActive.value = false
            isTournamentFinished.value = true
        }
    }
}