package com.example.bugs_android.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GameBlue = Color(0xFF0108CA)

@Composable
fun GameStartScreen( onStartClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Игра «Жуки»",
            fontSize = 28.sp
        )

        Text(
            text = "Готовы проверить свою реакцию?",
            fontSize = 18.sp
        )

        Button(
            onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = GameBlue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Начать игру",
                fontSize = 18.sp
            )
        }
    }
}


@Composable
fun GameResultsScreen( score: Int, hits: Int, misses: Int, accuracy: Int, onReplayClick: () -> Unit,
                       onChangePlayerClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .background(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Игра окончена!",
            fontSize = 22.sp
        )

        Text("Итоговые очки: $score", fontSize = 16.sp)
        Text("Попадания: $hits", fontSize = 16.sp)
        Text("Промахи: $misses", fontSize = 16.sp)
        Text("Точность: $accuracy%", fontSize = 16.sp)

        Button(
            onClick = onReplayClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GameBlue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Играть снова",
                fontSize = 18.sp
            )
        }

        Button(
            onClick = onChangePlayerClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = GameBlue,
                contentColor = Color.White
            )
        ) {
            Text("Сменить игрока", fontSize = 18.sp)
        }
    }
}

@Composable
fun GameStatsPanel(score: Int, hits: Int, misses: Int, timeLeft: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(
                color = Color(0xDDFFFFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Очки\n$score")
        Text("Попадания\n$hits")
        Text("Промахи\n$misses")
        Text("Время\n$timeLeft с")
    }
}