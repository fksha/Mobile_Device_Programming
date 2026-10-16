package com.example.bugs_android.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bugs_android.data.ScoreWithUsers

@Composable
fun RecordScreen(
    scores: List<ScoreWithUsers>
) {
    var selectedSpeed by remember { mutableStateOf<Int?>(null) }
    var selectedDuration by remember { mutableStateOf<Int?>(null) }

    var speedMenuExpanded by remember { mutableStateOf(false) }
    var durationMenuExpanded by remember { mutableStateOf(false) }

    // скорости, которые встречаются в рекордах.
    val availableSpeeds = scores
        .map { it.speed }
        .distinct()
        .sorted()

    //  длительности, которые встречаются в рекордах.
    val availableDurations = scores
        .map { it.roundDuration }
        .distinct()
        .sorted()

    // рекорды, подходящие под выбранные фильтры.
    val filteredScores = scores.filter { result ->
        (selectedSpeed == null || result.speed == selectedSpeed) &&
                (selectedDuration == null || result.roundDuration == selectedDuration)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Таблица рекордов",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Фильтр по скорости.
        Text("Скорость игры")

        Box {
            Button(
                onClick = { speedMenuExpanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0108CA),
                    contentColor = Color.White
                )
            ) {
                Text(selectedSpeed?.toString() ?: "Все скорости")
            }

            DropdownMenu(
                expanded = speedMenuExpanded,
                onDismissRequest = { speedMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Все скорости") },
                    onClick = {
                        selectedSpeed = null
                        speedMenuExpanded = false
                    }
                )

                availableSpeeds.forEach { speed ->
                    DropdownMenuItem(
                        text = { Text(speed.toString()) },
                        onClick = {
                            selectedSpeed = speed
                            speedMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Фильтр по длительности раунда.
        Text("Длительность раунда")

        Box {
            Button(
                onClick = { durationMenuExpanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0108CA),
                    contentColor = Color.White
                )
            ) {
                Text(
                    selectedDuration?.let { "$it сек." }
                        ?: "Любая длительность"
                )
            }

            DropdownMenu(
                expanded = durationMenuExpanded,
                onDismissRequest = { durationMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Любая длительность") },
                    onClick = {
                        selectedDuration = null
                        durationMenuExpanded = false
                    }
                )

                availableDurations.forEach { duration ->
                    DropdownMenuItem(
                        text = { Text("$duration сек.") },
                        onClick = {
                            selectedDuration = duration
                            durationMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список рекордов.
        if (filteredScores.isEmpty()) {
            Text("Нет результатов по выбранным фильтрам")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredScores) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = result.userName,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text("Очки: ${result.points}")
                            Text("Попадания: ${result.hits}")
                            Text("Промахи: ${result.misses}")
                            Text("Точность: ${result.accuracy}%")
                            Text("Скорость: ${result.speed}")
                            Text("Длительность: ${result.roundDuration} сек.")
                        }
                    }
                }
            }
        }
    }
}
