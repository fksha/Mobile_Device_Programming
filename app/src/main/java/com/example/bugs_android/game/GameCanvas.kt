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
import com.example.bugs_android.GameViewModel
import com.example.bugs_android.network.GoldRateRepository
import kotlin.math.roundToInt

private const val LOGICAL_WIDTH = 1000f
private const val LOGICAL_HEIGHT = 1000f
private const val TOP_PANEL_HEIGHT = 130f


@Composable
fun GameCanvas(userId: Int, viewModel: GameViewModel, onChangePlayer: () -> Unit) {
    //val bugs = remember { mutableStateListOf<Bug>() }
    val bugs = viewModel.bugs

    //val targets = remember { mutableStateMapOf<Int, Offset>() }
    val targets = viewModel.targets

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

    //val bonusPosition = remember { mutableStateOf<Offset?>(null) }//6

    //val isBonusActive = remember { mutableStateOf(false) }//6

    //var bonusTimeLeft by remember { mutableIntStateOf(0) }

    //6 наклон телефона по вертикали и горизонтали
    val tiltX = remember { mutableFloatStateOf(0f) }
    val tiltY = remember { mutableFloatStateOf(0f) }

    DisposableEffect(viewModel.isBonusActive, accelerometer) {
        if (!viewModel.isBonusActive || accelerometer == null) {
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
    LaunchedEffect(viewModel.isBonusActive) {
        if (viewModel.isBonusActive) {
            while (viewModel.bonusTimeLeft > 0) {
                delay(1000L)
                viewModel.decreaseBonusTime()
            }

            viewModel.deactivateBonus()
        }
    }

    val database = remember { GameDatabase.getDatabase(context) }

    val preferences = remember {
        context.getSharedPreferences(
            "game_settings",
            Context.MODE_PRIVATE
        )
    }

    //val isGameOver = remember { mutableStateOf(false) }

    //val isGameStarted = remember { mutableStateOf(false) }

    //val isCountingDown = remember { mutableStateOf(true) }

    //val countdown = remember { mutableIntStateOf(3) }

    //val gameSession = remember { mutableIntStateOf(0) }

    //var savedSession by remember { mutableIntStateOf(-1) }

    //
    // val nextBugId = remember { mutableIntStateOf(1) }

    val canvasWidth = remember { mutableIntStateOf(0) }

    val canvasHeight = remember { mutableIntStateOf(0) }

    val maxBugs = preferences.getInt("maxBugs", 5)

    val speedMultiplier = preferences.getInt("speed", 5) / 5f

    val roundDuration = preferences.getInt("roundDuration", 60)

    //7
    //var lastGoldBugTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val normalImage = ImageBitmap.imageResource(R.drawable.bug_normal)
    val fastImage = ImageBitmap.imageResource(R.drawable.bug_fast)
    val rareImage = ImageBitmap.imageResource(R.drawable.bug_rare)
    //7
    val goldImage = ImageBitmap.imageResource(R.drawable.bug_gold)

    //Появление бонуса каждые 15 сек
    LaunchedEffect(viewModel.gameSession, viewModel.isRoundRunning) {
        while (viewModel.isRoundRunning && !viewModel.isGameOver){
            delay(15_000L)

            if(viewModel.isRoundRunning && !viewModel.isGameOver){
                if (viewModel.bonusPosition == null){
                    val margin = 60f

                    val x = margin + Random.nextFloat() * (LOGICAL_WIDTH - 2 * margin)
                    val y = TOP_PANEL_HEIGHT + margin +
                            Random.nextFloat() * (LOGICAL_HEIGHT - TOP_PANEL_HEIGHT - 2 * margin)

                    viewModel.updateBonusPosition(Offset(x, y))
                }
            }
        }
    }

    // Обратный отсчёт перед началом игры
    LaunchedEffect(viewModel.isCountingDown, viewModel.countdown) {
        if (viewModel.isCountingDown && viewModel.countdown > 0) {
            delay(1000L)
            viewModel.tickCountdown()
        }
    }

    // Появление жуков с учётом настроек
    LaunchedEffect(
        canvasWidth.intValue,
        canvasHeight.intValue,
        viewModel.gameSession,
        viewModel.isGameOver,
        viewModel.isRoundRunning
    ) {
        while (
            canvasWidth.intValue > 0 &&
            canvasHeight.intValue > 0 &&
            !viewModel.isGameOver &&
            viewModel.isRoundRunning
        ) {
            if (bugs.size < maxBugs) {
                val id = viewModel.takeNextBugId()
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

    LaunchedEffect(viewModel.gameSession, viewModel.isGameOver, viewModel.isRoundRunning) {
        while (!viewModel.isGameOver && viewModel.isRoundRunning){
            delay(20_000L)

            if(viewModel.isGameOver || !viewModel.isRoundRunning){
                break
            }

            //Актуальный курс золота
            val rate = try{
                GoldRateRepository.getGoldRate()
            }catch (e: Exception){null}

            if(rate != null){
                val id = viewModel.takeNextBugId()
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
        viewModel.gameSession,
        viewModel.isGameOver,
        viewModel.isRoundRunning
    ) {
        var previousTime = 0L

        while (!viewModel.isGameOver && viewModel.isRoundRunning) {
            val currentTime = withFrameNanos { it }

            val deltaTime = if (previousTime == 0L) {
                0f
            } else {
                (currentTime - previousTime) / 1_000_000_000f
            }

            previousTime = currentTime

            for (index in bugs.indices) {
                val bug = bugs[index]

                if (viewModel.isBonusActive) {
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

//                val targetX = if(viewModel.isBonusActive){
//                    when {
//                        tiltX.floatValue > 1f -> LOGICAL_WIDTH - bug.size
//                        tiltX.floatValue < -1f -> 0f
//                        else -> target.x
//                    }
//                }
//                else{
//                    target.x
//                }
//
//                val targetY = if(viewModel.isBonusActive){
//                    when{
//                        tiltY.floatValue < -1f -> TOP_PANEL_HEIGHT
//                        tiltY.floatValue > 1f -> LOGICAL_HEIGHT - bug.size
//                        else -> target.y
//                    }
//                }
//                else{
//                    target.y
//                }

                val targetX = target.x
                val targetY = target.y

                val dx = targetX - bug.x
                val dy = targetY - bug.y

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

//    val score = remember { mutableIntStateOf(0) }

//    val hits = remember { mutableIntStateOf(0) }
//
//    val misses = remember { mutableIntStateOf(0) }

    val accuracy = if (viewModel.hits + viewModel.misses > 0) {
        viewModel.hits * 100 / (viewModel.hits + viewModel.misses)
    } else {
        0
    }

    LaunchedEffect(viewModel.isGameOver, viewModel.gameSession) {
        if (viewModel.isGameOver && viewModel.savedSession != viewModel.gameSession
        ) {
            database.scoreDao().insertScore(
                ScoreEntity(
                    userId = userId,
                    points = viewModel.score,
                    difficulty = preferences.getInt("difficulty", 1),
                    speed = preferences.getInt("speed", 5),
                    roundDuration = roundDuration,
                    hits = viewModel.hits,
                    misses = viewModel.misses,
                    accuracy = accuracy.toFloat(),
                    date = System.currentTimeMillis()
                )
            )

            viewModel.markSessionSaved()
        }
    }

    //val timeLeft = remember { mutableIntStateOf(roundDuration) }

    // Таймер раунда
    LaunchedEffect(
        viewModel.gameSession,
        viewModel.isRoundRunning
    ) {
        viewModel.prepareRound(roundDuration)

        if (!viewModel.isRoundRunning || viewModel.isGameOver) {
            return@LaunchedEffect
        }

        while (
            viewModel.timeLeft > 0 &&
            !viewModel.isGameOver &&
            viewModel.isRoundRunning
        ) {
            delay(1000L)
            viewModel.tickTimer()
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
                        if (viewModel.isGameOver || !viewModel.isRoundRunning) {
                            return@detectTapGestures
                        }

                        val tapX =
                            tapOffset.x * LOGICAL_WIDTH / size.width

                        val tapY =
                            tapOffset.y * LOGICAL_HEIGHT / size.height

                        //проверяем нажатие на бонус
                        val bonus = viewModel.bonusPosition

                        if (bonus != null && kotlin.math.abs(tapX - bonus.x) < 60f &&
                            kotlin.math.abs(tapY - bonus.y) < 60f){

                            viewModel.updateBonusPosition(null)
                            viewModel.activateBonus()

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
                            viewModel.addHit(hitBug.points)

                            currentBugs.value.remove(hitBug)
                            currentTargets.value.remove(hitBug.id)
                        } else {
                            // Промах
                            viewModel.addMiss()
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
            viewModel.bonusPosition?.let{position ->
                val paint = android.graphics.Paint().apply {
                    textSize = 110f * minOf(size.width / LOGICAL_WIDTH, size.height / LOGICAL_HEIGHT )
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "🌀",
                    position.x * size.width / LOGICAL_WIDTH,
                    position.y * size.height / LOGICAL_HEIGHT,
                    paint

                )
            }
        }

        // Панель статистики во время игры
        if (viewModel.isRoundRunning && !viewModel.isGameOver) {
            GameStatsPanel(
                score = viewModel.score,
                hits = viewModel.hits,
                misses = viewModel.misses,
                timeLeft = viewModel.timeLeft
            )
        }

        // Начальный экран

       /* if (!isGameStarted.value && !viewModel.isCountingDown && !isGameOver.value) {
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
        if (viewModel.isCountingDown) {
            Text(
                text = viewModel.countdown.toString(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                fontSize = 60.sp,
                color = Color(0xFF0108CA)
            )
        }

        // Экран результатов
        if (viewModel.isGameOver) {
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                GameResultsScreen(
                    score = viewModel.score,
                    hits = viewModel.hits,
                    misses = viewModel.misses,
                    accuracy = accuracy,
                    onReplayClick = {
                        viewModel.updateBonusPosition(null)
                        viewModel.deactivateBonus()
                        tiltX.floatValue = 0f
                        tiltY.floatValue = 0f

                        viewModel.replay(roundDuration)
                    } ,
                    onChangePlayerClick = onChangePlayer
                )
            }
        }
    }
}