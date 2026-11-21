
package com.example.bugs_android.tournament

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bugs_android.data.TournamentResultEntity
import com.example.bugs_android.data.TournamentSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TournamentResultsScreen(
    tournaments: List<TournamentSummary>,
    selectedTournamentId: String?,
    results: List<TournamentResultEntity>,
    onTournamentClick: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat(
        "dd.MM.yyyy HH:mm",
        Locale.getDefault()
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        item {
            Text(
                text = "История турниров",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (tournaments.isEmpty()) {
            item {
                Text("Завершённых турниров пока нет.")
            }
        } else {
            itemsIndexed(tournaments) { index, tournament ->

                val isExpanded =
                    selectedTournamentId == tournament.tournamentId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable {
                            onTournamentClick(
                                if (isExpanded) "" else tournament.tournamentId
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Турнир №${tournaments.size - index}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = dateFormat.format(
                                Date(tournament.tournamentDate)
                            )
                        )

                        Text("Участников: ${tournament.participantCount}")

                        Text("Лучший результат: ${tournament.bestScore}")

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Результаты участников",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (results.isEmpty()) {
                                Text("Загрузка результатов...")
                            } else {
                                results.forEachIndexed { resultIndex, result ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = buildString {
                                                append("${resultIndex + 1}. ${result.userName}")

                                                if (result.isDeleted) {
                                                    append(" (удалён)")
                                                }
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (result.isDeleted) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )

                                        Text("Очки: ${result.score}")
                                        Text("Точность: ${result.accuracy}%")
                                        Text("Попадания: ${result.hits}")
                                        Text("Промахи: ${result.misses}")
                                    }

                                    if (resultIndex != results.lastIndex) {
                                        Spacer(
                                            modifier = Modifier.height(4.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text("Нажмите, чтобы посмотреть результаты")
                        }
                    }
                }
            }
        }
    }
}
