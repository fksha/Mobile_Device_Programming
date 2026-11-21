
package com.example.bugs_android.tournament

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TournamentReadyScreen(
    playerName: String,
    currentRound: Int,
    totalRounds: Int,
    onStartClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isLandscape = maxHeight < 400.dp

        val screenPadding = if (isLandscape) 8.dp else 24.dp
        val titleSize = if (isLandscape) 24.sp else 30.sp
        val roundSize = if (isLandscape) 16.sp else 20.sp
        val playerSize = if (isLandscape) 21.sp else 26.sp
        val smallSpace = if (isLandscape) 4.dp else 8.dp
        val mediumSpace = if (isLandscape) 10.dp else 24.dp
        val largeSpace = if (isLandscape) 12.dp else 40.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Турнир",
                fontSize = titleSize,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(mediumSpace))

            Text(
                text = "Раунд $currentRound из $totalRounds",
                fontSize = roundSize
            )

            Spacer(modifier = Modifier.height(mediumSpace))

            Text(
                text = "Сейчас играет:",
                fontSize = roundSize
            )

            Spacer(modifier = Modifier.height(smallSpace))

            Text(
                text = playerName,
                fontSize = playerSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(largeSpace))

            Button(
                onClick = onStartClick
            ) {
                Text(
                    text = "Играть",
                    fontSize = if (isLandscape) 16.sp else 18.sp
                )
            }
        }
    }
}
