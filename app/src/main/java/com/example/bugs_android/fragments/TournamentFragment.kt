
package com.example.bugs_android.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.bugs_android.GameViewModel
import com.example.bugs_android.data.GameDatabase
import com.example.bugs_android.data.TournamentResultEntity
import com.example.bugs_android.data.UserEntity
import com.example.bugs_android.game.GameCanvas
import com.example.bugs_android.tournament.TournamentEntryScreen
import com.example.bugs_android.tournament.TournamentReadyScreen
import com.example.bugs_android.tournament.TournamentSettings
import com.example.bugs_android.tournament.TournamentViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TournamentFragment : Fragment() {

    private val tournamentViewModel: TournamentViewModel by activityViewModel()
    private val gameViewModel: GameViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val database = GameDatabase.getDatabase(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {

                var users by remember {
                    mutableStateOf<List<UserEntity>>(emptyList())
                }

                var selectedUserIds by remember {
                    mutableStateOf<Set<Int>>(emptySet())
                }

                val coroutineScope = rememberCoroutineScope()

                val preferences = remember {
                    requireContext().getSharedPreferences(
                        "game_settings",
                        Context.MODE_PRIVATE
                    )
                }

                fun readTournamentSettings(): TournamentSettings {
                    return TournamentSettings(
                        roundDuration = preferences.getInt("roundDuration", 60),
                        speed = preferences.getInt("speed", 5),
                        maxBugs = preferences.getInt("maxBugs", 5)
                    )
                }

                // Загружаем зарегистрированных игроков
                LaunchedEffect(Unit) {
                    users = database.userDao().getAllUsers()
                }

                // Запускаем раунд после нажатия кнопки "Играть"
                LaunchedEffect(
                    tournamentViewModel.isTournamentActive.value,
                    tournamentViewModel.isRoundStarted.value,
                    tournamentViewModel.currentPlayer?.id,
                    tournamentViewModel.currentPlayerIndex.intValue,
                    tournamentViewModel.tournamentId.value
                ) {
                    if (
                        tournamentViewModel.isTournamentActive.value &&
                        tournamentViewModel.isRoundStarted.value
                    ) {
                        tournamentViewModel.currentPlayer?.let { player ->

                            val roundKey =
                                "${tournamentViewModel.tournamentId.value}_${tournamentViewModel.currentPlayerIndex.intValue}"

                            gameViewModel.startTournamentRound(
                                userId = player.id,
                                roundKey = roundKey
                            )
                        }
                    }
                }

                // Сохраняем результаты после завершения последнего раунда
                LaunchedEffect(
                    tournamentViewModel.isTournamentFinished.value,
                    tournamentViewModel.tournamentId.value
                ) {
                    val tournamentId = tournamentViewModel.tournamentId.value

                    if (
                        tournamentViewModel.isTournamentFinished.value &&
                        tournamentId.isNotBlank() &&
                        tournamentViewModel.results.isNotEmpty() &&
                        !tournamentViewModel.areResultsSaved.value
                    ) {
                        val entities = tournamentViewModel.results.map { result ->
                            TournamentResultEntity(
                                tournamentId = tournamentId,
                                tournamentDate = tournamentViewModel.tournamentDate.value,
                                userId = result.userId,
                                userName = result.userName,
                                score = result.score,
                                hits = result.hits,
                                misses = result.misses,
                                accuracy = result.accuracy
                            )
                        }

                        database.tournamentResultDao().insertResults(entities)

                        tournamentViewModel.markResultsSaved()
                    }
                }

                // Выбор участников
                if (
                    !tournamentViewModel.isTournamentActive.value &&
                    !tournamentViewModel.isTournamentFinished.value
                ) {

                    TournamentEntryScreen(
                        users = users,
                        selectedUserIds = selectedUserIds,

                        onToggleUser = { user ->
                            selectedUserIds = if (user.id in selectedUserIds) {
                                selectedUserIds - user.id
                            } else {
                                selectedUserIds + user.id
                            }
                        },

                        onRegisterUser = { name ->
                            coroutineScope.launch {
                                val existingUser =
                                    database.userDao().getUserByName(name)

                                if (existingUser != null) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Игрок с таким именем уже существует!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    database.userDao().insertUser(
                                        UserEntity(name = name)
                                    )

                                    users = database.userDao().getAllUsers()

                                    Toast.makeText(
                                        requireContext(),
                                        "Игрок успешно зарегистрирован!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },

                        onDeleteUser = { user ->
                            coroutineScope.launch {
                                // Помечаем результаты турниров как удалённые
                                database.tournamentResultDao()
                                    .markPlayerAsDeleted(user.id)

                                database.userDao().deleteUser(user)

                                users = database.userDao().getAllUsers()
                                selectedUserIds = selectedUserIds - user.id

                                Toast.makeText(
                                    requireContext(),
                                    "Игрок удалён",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },

                        onStartTournament = { selectedUsers ->
                            tournamentViewModel.startTournament(
                                selectedUsers,
                                readTournamentSettings()
                            )
                        }
                    )
                }

                // Турнир активен, но игрок ещё не начал свой раунд
                else if (
                    tournamentViewModel.isTournamentActive.value &&
                    !tournamentViewModel.isRoundStarted.value
                ) {

                    tournamentViewModel.currentPlayer?.let { player ->
                        TournamentReadyScreen(
                            playerName = player.name,
                            currentRound =
                                tournamentViewModel.currentPlayerIndex.intValue + 1,
                            totalRounds = tournamentViewModel.participants.size,

                            onStartClick = {
                                tournamentViewModel.startCurrentRound()
                            }
                        )
                    }
                }

                // Игрок нажал "Играть"
                else if (
                    tournamentViewModel.isTournamentActive.value &&
                    tournamentViewModel.isRoundStarted.value
                ) {

                    tournamentViewModel.currentPlayer?.let { player ->
                        GameCanvas(
                            userId = player.id,
                            viewModel = gameViewModel,
                            isTournament = true,

                            onChangePlayer = {
                                // Смена игрока в обычной игре
                            },

                            onTournamentRoundFinished = {
                                tournamentViewModel.finishCurrentRound(
                                    score = gameViewModel.score,
                                    hits = gameViewModel.hits,
                                    misses = gameViewModel.misses
                                )
                            },
                            tournamentSettings = tournamentViewModel.settings.value
                        )
                    }
                }

                // Все участники завершили свои раунды
                else if (tournamentViewModel.isTournamentFinished.value) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(text = "Турнир завершён!")

                        Spacer(modifier = Modifier.height(16.dp))

                        if (tournamentViewModel.areResultsSaved.value) {

                            // Повторить турнир с теми же участниками
                            Button(
                                onClick = {
                                    val previousParticipants = tournamentViewModel.participants.toList()

                                    tournamentViewModel.startTournament(
                                        previousParticipants,
                                        readTournamentSettings()
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Повторить турнир")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Выбрать новых участников
                            Button(
                                onClick = {
                                    selectedUserIds = emptySet()
                                    tournamentViewModel.resetTournament()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Выбрать новых участников")
                            }

                        } else {
                            Text("Сохраняем результаты турнира...")
                        }
                    }
                }
            }
        }
    }
}
