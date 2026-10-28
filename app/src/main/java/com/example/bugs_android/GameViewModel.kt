package com.example.bugs_android

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.example.bugs_android.model.Bug

class GameViewModel : ViewModel() {

    // Игрок

    var selectedUserId by mutableStateOf<Int?>(null)
        private set

    fun selectUser(userId: Int) {
        selectedUserId = userId
    }

    fun clearSelectedUser() {
        selectedUserId = null
    }


    // Состояние игрового экрана и раунда

    var isGameStarted by mutableStateOf(false)
        private set

    var isRoundRunning by mutableStateOf(false)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var isCountingDown by mutableStateOf(true)
        private set

    var countdown by mutableIntStateOf(3)
        private set


    // Игровые объекты

    val bugs = mutableStateListOf<Bug>()

    val targets = mutableStateMapOf<Int, Offset>()

    var bonusPosition by mutableStateOf<Offset?>(null)
        private set

    var isBonusActive by mutableStateOf(false)
        private set

    var bonusTimeLeft by mutableIntStateOf(0)
        private set


    fun updateBonusPosition(position: Offset?) {
        bonusPosition = position
    }
    fun activateBonus() {
        isBonusActive = true
        bonusTimeLeft = 5
    }

    fun deactivateBonus() {
        isBonusActive = false
        bonusTimeLeft = 0
    }

    fun decreaseBonusTime() {
        if (bonusTimeLeft > 0) {
            bonusTimeLeft--
        }
    }

    private var preparedSession = -1

    private var nextBugId by mutableIntStateOf(1)

    fun takeNextBugId(): Int {
        return nextBugId++
    }


    // Статистика

    var score by mutableIntStateOf(0)
        private set

    var hits by mutableIntStateOf(0)
        private set

    var misses by mutableIntStateOf(0)
        private set

    fun addHit(points: Int) {
        score += points
        hits++
    }

    fun addMiss() {
        score -= 5
        misses++
    }

    fun resetStats() {
        score = 0
        hits = 0
        misses = 0
    }


    // Запуск игры и смена игрока

    fun startGame(userId: Int) {
        selectedUserId = userId
        isGameStarted = true
        isGameOver = false
        isRoundRunning = false
        startCountdown()
    }

    fun changePlayer() {
        selectedUserId = null
        isGameStarted = false
        isRoundRunning = false
        isGameOver = false
        isCountingDown = true
        countdown = 3

        resetStats()
        bugs.clear()
        targets.clear()

        gameSession++
        nextBugId = 1

        bonusPosition = null
        deactivateBonus()
    }


    // Обратный отсчёт

    fun startCountdown() {
        countdown = 3
        isCountingDown = true
        isRoundRunning = false
        isGameOver = false
    }

    fun tickCountdown() {
        if (countdown > 1) {
            countdown--
        } else {
            countdown = 0
            isCountingDown = false
            isRoundRunning = true
        }
    }


    // Таймер раунда

    var timeLeft by mutableIntStateOf(60)
        private set

    var gameSession by mutableIntStateOf(0)
        private set

    fun startRound(duration: Int) {
        timeLeft = duration
    }

    fun prepareRound(duration: Int) {
        if (preparedSession != gameSession) {
            timeLeft = duration
            preparedSession = gameSession
        }
    }

    fun tickTimer() {
        if (timeLeft > 0) {
            timeLeft--
        }

        if (timeLeft == 0) {
            finishGame()
        }
    }

    fun finishGame() {
        isGameOver = true
        isRoundRunning = false
    }


    // Повторная игра

    fun replay(duration: Int) {
        resetStats()

        bugs.clear()
        targets.clear()

        isGameOver = false
        isRoundRunning = false
        countdown = 3
        isCountingDown = true
        timeLeft = duration

        gameSession++
        nextBugId = 1

        bonusPosition = null
        deactivateBonus()
    }

    var savedSession by mutableIntStateOf(-1)
        private set

    fun markSessionSaved() {
        savedSession = gameSession
    }


}