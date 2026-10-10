package com.example.bugs_android.game

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bugs_android.R
import com.example.bugs_android.enumC.BugType
import com.example.bugs_android.logic.createBug
import com.example.bugs_android.model.Bug
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.delay

private const val LOGICAL_WIDTH = 1000f
private const val LOGICAL_HEIGHT = 1000f
private const val TOP_PANEL_HEIGHT = 130f

@Composable
fun GameCanvas() {
    val bugs = remember { mutableStateListOf<Bug>() }

    val targets = remember { mutableStateMapOf<Int, Offset>() }

    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "game_settings",
            Context.MODE_PRIVATE
        )
    }

    val isGameOver = remember { mutableStateOf(false) }

    val isGameStarted = remember { mutableStateOf(false) }

    val isCountingDown = remember { mutableStateOf(false) }

    val countdown = remember { mutableIntStateOf(3) }

    val gameSession = remember { mutableIntStateOf(0) }

    val nextBugId = remember { mutableIntStateOf(1) }

    val canvasWidth = remember { mutableIntStateOf(0) }

    val canvasHeight = remember { mutableIntStateOf(0) }

    val maxBugs = preferences.getInt("maxBugs", 5)

    val speedMultiplier = preferences.getInt("speed", 5) / 5f

    val roundDuration = preferences.getInt("roundDuration", 60)


    val normalImage = ImageBitmap.imageResource(R.drawable.bug_normal)
    val fastImage = ImageBitmap.imageResource(R.drawable.bug_fast)
    val rareImage = ImageBitmap.imageResource(R.drawable.bug_rare)

    // Обратный отсчёт перед началом игры
    LaunchedEffect(isCountingDown.value, countdown.intValue) {
        if (isCountingDown.value && countdown.intValue > 0) {
            delay(1000L)

            if (countdown.intValue > 1) {
                countdown.intValue--
            } else {
                countdown.intValue = 0
                isCountingDown.value = false
                isGameStarted.value = true
            }
        }
    }

    // Появление жуков с учётом настроек
    LaunchedEffect(
        canvasWidth.intValue,
        canvasHeight.intValue,
        gameSession.intValue,
        isGameOver.value,
        isGameStarted.value
    ) {
        while (
            canvasWidth.intValue > 0 &&
            canvasHeight.intValue > 0 &&
            !isGameOver.value &&
            isGameStarted.value
        ) {
            if (bugs.size < maxBugs) {
                val id = nextBugId.intValue++
                val type = BugType.entries.random()

                val bug = createBug(
                    type = type,
                    id = id,
                    x = 0f,
                    y = 0f
                )

                // Случайное начальное положение жука
                bug.x = Random.nextFloat() *
                        (LOGICAL_WIDTH - bug.size).coerceAtLeast(0f)

                bug.y = TOP_PANEL_HEIGHT +
                        Random.nextFloat() *
                        (LOGICAL_HEIGHT - TOP_PANEL_HEIGHT - bug.size)
                            .coerceAtLeast(0f)

                bugs.add(bug)

                // Цель, к которой жук будет двигаться
                targets[bug.id] = createRandomTarget(bug.size)
            }

            delay(500L)
        }
    }

    // Движение жуков и плавный поворот к цели
    LaunchedEffect(
        gameSession.intValue,
        isGameOver.value,
        isGameStarted.value
    ) {
        var previousTime = 0L

        while (!isGameOver.value && isGameStarted.value) {
            val currentTime = withFrameNanos { it }

            val deltaTime = if (previousTime == 0L) {
                0f
            } else {
                (currentTime - previousTime) / 1_000_000_000f
            }

            previousTime = currentTime

            for (index in bugs.indices) {
                val bug = bugs[index]
                val target = targets[bug.id] ?: continue

                val dx = target.x - bug.x
                val dy = target.y - bug.y

                val movementAngle = Math.toDegrees(
                    atan2(dy, dx).toDouble()
                ).toFloat()

                val angleDiff =
                    ((movementAngle - bug.angle + 540f) % 360f) - 180f

                val smoothAngle = bug.angle + angleDiff * 0.12f

                val distance = sqrt(dx * dx + dy * dy)
                val step = bug.speed * speedMultiplier * deltaTime

                if (distance <= step || distance < 0.1f) {
                    // Жук достиг цели — выбираем следующую
                    bugs[index] = bug.copy(
                        x = target.x,
                        y = target.y,
                        angle = smoothAngle
                    )

                    targets[bug.id] = createRandomTarget(bug.size)

                } else {
                    // Перемещаем жука в сторону цели
                    bugs[index] = bug.copy(
                        x = bug.x + dx / distance * step,
                        y = bug.y + dy / distance * step,
                        angle = smoothAngle
                    )
                }
            }
        }
    }

    val score = remember { mutableIntStateOf(0) }

    val hits = remember { mutableIntStateOf(0) }

    val misses = remember { mutableIntStateOf(0) }

    val accuracy = if (hits.intValue + misses.intValue > 0) {
        hits.intValue * 100 / (hits.intValue + misses.intValue)
    } else {
        0
    }

    val timeLeft = remember { mutableIntStateOf(roundDuration) }

    // Таймер раунда
    LaunchedEffect(
        roundDuration,
        gameSession.intValue,
        isGameStarted.value
    ) {
        if (!isGameStarted.value) {
            return@LaunchedEffect
        }

        timeLeft.intValue = roundDuration
        isGameOver.value = false

        while (timeLeft.intValue > 0 && !isGameOver.value) {
            delay(1000L)
            timeLeft.intValue--
        }

        if (timeLeft.intValue == 0) {
            isGameOver.value = true
        }
    }

    val currentBugs = rememberUpdatedState(bugs)
    val currentTargets = rememberUpdatedState(targets)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Игровое поле
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    canvasWidth.intValue = size.width
                    canvasHeight.intValue = size.height
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        if (isGameOver.value || !isGameStarted.value) {
                            return@detectTapGestures
                        }

                        val tapX =
                            tapOffset.x * LOGICAL_WIDTH / size.width

                        val tapY =
                            tapOffset.y * LOGICAL_HEIGHT / size.height

                        // Нажатие на верхнюю панель не считается промахом
                        if (tapY < TOP_PANEL_HEIGHT) {
                            return@detectTapGestures
                        }

                        val hitBug = currentBugs.value.lastOrNull { bug ->
                            tapX >= bug.x &&
                                    tapX <= bug.x + bug.size &&
                                    tapY >= bug.y &&
                                    tapY <= bug.y + bug.size
                        }

                        if (hitBug != null) {
                            // Попадание по жуку
                            score.intValue += hitBug.points
                            hits.intValue++

                            currentBugs.value.remove(hitBug)
                            currentTargets.value.remove(hitBug.id)
                        } else {
                            // Промах
                            score.intValue -= 5
                            misses.intValue++
                        }
                    }
                }
        ) {
            drawBugs(
                bugs = bugs,
                normalImage = normalImage,
                fastImage = fastImage,
                rareImage = rareImage
            )
        }

        // Панель статистики во время игры
        if (isGameStarted.value && !isGameOver.value) {
            GameStatsPanel(
                score = score.intValue,
                hits = hits.intValue,
                misses = misses.intValue,
                timeLeft = timeLeft.intValue
            )
        }

        // Начальный экран

        if (!isGameStarted.value && !isCountingDown.value && !isGameOver.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                GameStartScreen(
                    onStartClick = {
                        countdown.intValue = 3
                        isCountingDown.value = true
                    }
                )
            }
        }

        // Обратный отсчёт
        if (isCountingDown.value) {
            Text(
                text = countdown.intValue.toString(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                fontSize = 60.sp,
                color = Color(0xFF0108CA)
            )
        }

        // Экран результатов
        if (isGameOver.value) {
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                GameResultsScreen(
                    score = score.intValue,
                    hits = hits.intValue,
                    misses = misses.intValue,
                    accuracy = accuracy,
                    onReplayClick = {
                        score.intValue = 0
                        hits.intValue = 0
                        misses.intValue = 0

                        bugs.clear()
                        targets.clear()
                        nextBugId.intValue = 1

                        isGameOver.value = false
                        isGameStarted.value = false
                        countdown.intValue = 3
                        isCountingDown.value = true

                        gameSession.intValue++
                    }
                )
            }
        }
    }
}