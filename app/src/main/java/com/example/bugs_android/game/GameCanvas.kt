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
import com.example.bugs_android.data.GameDatabase
import com.example.bugs_android.data.ScoreEntity
import com.example.bugs_android.enumC.BugType
import com.example.bugs_android.logic.createBug
import com.example.bugs_android.model.Bug
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.runtime.DisposableEffect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.bugs_android.network.GoldRateRepository
import kotlin.math.roundToInt

private const val LOGICAL_WIDTH = 1000f
private const val LOGICAL_HEIGHT = 1000f
private const val TOP_PANEL_HEIGHT = 130f

private const val GOLD_BUG_INTERVAL = 20_000L

@Composable
fun GameCanvas(userId: Int, onChangePlayer: () -> Unit) {
    val bugs = remember { mutableStateListOf<Bug>() }

    val targets = remember { mutableStateMapOf<Int, Offset>() }

    val context = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    val soundId = remember { mutableIntStateOf(0) }
    val soundLoaded = remember { mutableStateOf(false) }
    var lastSoundTime by remember { mutableLongStateOf(0L) }

    DisposableEffect(soundPool) {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                soundId.intValue = sampleId
                soundLoaded.value = true
            }
        }

        soundPool.load(context, R.raw.sound, 1)

        onDispose {
            soundPool.release()
        }
    }

    //6 SensorManager позволяет получать данные от датчиков телефона
    //TYPE_ACCELEROMETER выбирает акселерометр.
    //getDefaultSensor() возвращает доступный датчик или null, если его нет.
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val bonusPosition = remember { mutableStateOf<Offset?>(null) }//6

    val isBonusActive = remember { mutableStateOf(false) }//6

    var bonusTimeLeft by remember { mutableIntStateOf(0) }

    //6 наклон телефона по вертикали и горизонтали
    val tiltX = remember { mutableFloatStateOf(0f) }
    val tiltY = remember { mutableFloatStateOf(0f) }

    DisposableEffect(isBonusActive.value, accelerometer) {
        if (!isBonusActive.value || accelerometer == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val newX = event.values[0]
                    val newY = event.values[1]

                    val changeX = kotlin.math.abs(newX - tiltX.floatValue)
                    val changeY = kotlin.math.abs(newY - tiltY.floatValue)

                    if (changeX > 0.3f || changeY > 0.3f) {
                        val currentTime = System.currentTimeMillis()

                        if (soundLoaded.value &&
                            currentTime - lastSoundTime >= 1000L
                        ) {
                            soundPool.play(
                                soundId.intValue,
                                1f,
                                1f,
                                1,
                                0,
                                1f
                            )

                            lastSoundTime = currentTime
                        }
                    }

                    tiltX.floatValue = newX
                    tiltY.floatValue = newY
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int
                ) {
                }
            }

            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    //дизактивация бонуса
    LaunchedEffect(isBonusActive.value) {
        if (isBonusActive.value) {
            bonusTimeLeft = 5

            while (bonusTimeLeft > 0) {
                delay(1000L)
                bonusTimeLeft--
            }

            isBonusActive.value = false
        }
    }

    val database = remember { GameDatabase.getDatabase(context) }

    val preferences = remember {
        context.getSharedPreferences(
            "game_settings",
            Context.MODE_PRIVATE
        )
    }

    val isGameOver = remember { mutableStateOf(false) }

    val isGameStarted = remember { mutableStateOf(false) }

    val isCountingDown = remember { mutableStateOf(true) }

    val countdown = remember { mutableIntStateOf(3) }

    val gameSession = remember { mutableIntStateOf(0) }

    var savedSession by remember { mutableIntStateOf(-1) }

    val nextBugId = remember { mutableIntStateOf(1) }

    val canvasWidth = remember { mutableIntStateOf(0) }

    val canvasHeight = remember { mutableIntStateOf(0) }

    val maxBugs = preferences.getInt("maxBugs", 5)

    val speedMultiplier = preferences.getInt("speed", 5) / 5f

    val roundDuration = preferences.getInt("roundDuration", 60)

    //7
    var lastGoldBugTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val normalImage = ImageBitmap.imageResource(R.drawable.bug_normal)
    val fastImage = ImageBitmap.imageResource(R.drawable.bug_fast)
    val rareImage = ImageBitmap.imageResource(R.drawable.bug_rare)
    //7
    val goldImage = ImageBitmap.imageResource(R.drawable.bug_gold)

    //Появление бонуса каждые 15 сек
    LaunchedEffect(gameSession.intValue, isGameStarted.value) {
        while (isGameStarted.value && !isGameOver.value){
            delay(15_000L)

            if(isGameStarted.value && !isGameOver.value){
                if (bonusPosition.value == null){
                    val margin = 60f

                    val x = margin + Random.nextFloat() * (LOGICAL_WIDTH - 2 * margin)
                    val y = TOP_PANEL_HEIGHT + margin +
                            Random.nextFloat() * (LOGICAL_HEIGHT - TOP_PANEL_HEIGHT - 2 * margin)
                    bonusPosition.value = Offset(x, y)
                }
            }
        }
    }

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
                //val type = BugType.entries.random()
                val type = when (Random.nextInt(100)) {
                    in 0..50 -> BugType.NORMAL
                    in 51..81 -> BugType.FAST
                    else -> BugType.RARE
                }

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

    LaunchedEffect(gameSession.intValue, isGameOver.value, isGameStarted.value) {
        while (!isGameOver.value && isGameStarted.value){
            delay(20_000L)

            if(isGameOver.value || !isGameStarted.value){
                break
            }

            //Актуальный курс золота
            val rate = try{
                GoldRateRepository.getGoldRate()
            }catch (e: Exception){null}

            if(rate != null){
                val id = nextBugId.intValue++
                val x = Random.nextFloat() *
                        (LOGICAL_WIDTH - 220f).coerceAtLeast(0f)

                val y = TOP_PANEL_HEIGHT +
                        Random.nextFloat() *
                        (LOGICAL_HEIGHT - TOP_PANEL_HEIGHT - 220f)
                            .coerceAtLeast(0f)
                // 1/100 курса золота превращаем в игровые очки
                val points = (rate.buy / 100.0).roundToInt()

                val goldBug = createBug(
                    type = BugType.GOLD,
                    id = id,
                    x = x,
                    y = y
                ).copy(points = points)

                bugs.add(goldBug)
                targets[goldBug.id] = createRandomTarget(goldBug.size)

            }

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

                if (isBonusActive.value) {
                    val tiltSpeed = 350f
                    val dx = -tiltX.floatValue * tiltSpeed * speedMultiplier * deltaTime
                    val dy = tiltY.floatValue * tiltSpeed * speedMultiplier * deltaTime

                    bugs[index] = bug.copy(
                        x = (bug.x + dx).coerceIn(
                            0f,
                            LOGICAL_WIDTH - bug.size
                        ),
                        y = (bug.y + dy).coerceIn(
                            TOP_PANEL_HEIGHT,
                            LOGICAL_HEIGHT - bug.size
                        )
                    )

                    continue
                }

                val target = targets[bug.id] ?: continue

//                val dx = target.x - bug.x
//                val dy = target.y - bug.y

                val targetX = if(isBonusActive.value){
                    when {
                        tiltX.floatValue > 1f -> LOGICAL_WIDTH - bug.size
                        tiltX.floatValue < -1f -> 0f
                        else -> target.x
                    }
                }
                else{
                    target.x
                }

                val targetY = if(isBonusActive.value){
                    when{
                        tiltY.floatValue < -1f -> TOP_PANEL_HEIGHT
                        tiltY.floatValue > 1f -> LOGICAL_HEIGHT - bug.size
                        else -> target.y
                    }
                }
                else{
                    target.y
                }

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
                    // Жук достиг цели да
                    bugs[index] = bug.copy(
                        x = targetX,
                        y = targetY,
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

    LaunchedEffect(isGameOver.value, gameSession.intValue) {
        if (isGameOver.value && savedSession != gameSession.intValue
        ) {
            database.scoreDao().insertScore(
                ScoreEntity(
                    userId = userId,
                    points = score.intValue,
                    difficulty = preferences.getInt("difficulty", 1),
                    speed = preferences.getInt("speed", 5),
                    roundDuration = roundDuration,
                    hits = hits.intValue,
                    misses = misses.intValue,
                    accuracy = accuracy.toFloat(),
                    date = System.currentTimeMillis()
                )
            )

            savedSession = gameSession.intValue
        }
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

                        //проверяем нажатие на бонус
                        val bonus = bonusPosition.value

                        if (bonus != null && kotlin.math.abs(tapX - bonus.x) < 60f &&
                            kotlin.math.abs(tapY - bonus.y) < 60f){
                            bonusPosition.value = null
                            isBonusActive.value = true
                            bonusTimeLeft = 5

                            if (soundLoaded.value) {
                                soundPool.play(
                                    soundId.intValue,
                                    1f,
                                    1f,
                                    1,
                                    0,
                                    1f
                                )
                            }

                            return@detectTapGestures
                        }

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
                rareImage = rareImage,
                goldImage = goldImage
            )

            //БОНУС
            bonusPosition.value?.let{position ->
                drawContext.canvas.nativeCanvas.drawText(
                    "🌀",
                    position.x * size.width / LOGICAL_WIDTH,
                    position.y * size.height / LOGICAL_HEIGHT,
                    android.graphics.Paint().apply {
                        textSize = 110f * size.width / LOGICAL_WIDTH
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                )
            }
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

       /* if (!isGameStarted.value && !isCountingDown.value && !isGameOver.value) {
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
*/        // Обратный отсчёт
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

                        bonusPosition.value = null
                        isBonusActive.value = false
                        tiltX.floatValue = 0f
                        tiltY.floatValue = 0f

                        nextBugId.intValue = 1

                        isGameOver.value = false
                        isGameStarted.value = false
                        countdown.intValue = 3
                        isCountingDown.value = true

                        gameSession.intValue++
                    } ,
                    onChangePlayerClick = onChangePlayer
                )
            }
        }
    }
}