package com.example.bugs_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.bugs_android.data.GameDatabase
import com.example.bugs_android.data.UserEntity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.bugs_android.GameViewModel
import com.example.bugs_android.TournamentViewModel
import com.example.bugs_android.game.GameCanvas
import com.example.bugs_android.game.TournamentEntryScreen
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TournamentFragment : Fragment() {
    private val tournamentViewModel: TournamentViewModel by activityViewModel()
    private val gameViewModel: GameViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val database = GameDatabase.getDatabase(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {

                var users by remember { mutableStateOf<List<UserEntity>>(emptyList()) }

                var selectedUserIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    users = database.userDao().getAllUsers()

                }
                LaunchedEffect(tournamentViewModel.isTournamentActive.value,
                    tournamentViewModel.currentPlayer?.id
                ) {
                    if (tournamentViewModel.isTournamentActive.value) {
                        tournamentViewModel.currentPlayer?.let { player ->
                            gameViewModel.startTournamentRound(player.id)
                        }
                    }
                }

                if(!tournamentViewModel.isTournamentActive.value && !tournamentViewModel.isTournamentFinished.value)
                {
                    TournamentEntryScreen(
                        users = users,
                        selectedUserIds = selectedUserIds,

                        onToggleUser = {user ->
                            // Выбор или снятие выбора игрока
                            selectedUserIds = if(user.id in selectedUserIds){
                                selectedUserIds - user.id
                            }
                            else{
                                selectedUserIds + user.id
                            }
                        },

                        onRegisterUser = {name ->
                            coroutineScope.launch {
                                val existingUser = database.userDao().getUserByName(name)

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

                        onDeleteUser = {user ->
                            coroutineScope.launch {
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

                        onStartTournament ={ selectedUsers ->
                            tournamentViewModel.startTournament(selectedUsers)
                        }

                    )

                }
                else if (tournamentViewModel.isTournamentActive.value){
                    GameCanvas(
                        userId = tournamentViewModel.currentPlayer!!.id,
                        viewModel = gameViewModel,
                        isTournament = true,
                        onChangePlayer = {
                            //tournamentViewModel.isTournamentActive.value = false
                        },
                        onTournamentRoundFinished = {
                            tournamentViewModel.finishCurrentRound(
                                score = gameViewModel.score,
                                hits = gameViewModel.hits,
                                misses = gameViewModel.misses
                            )
                        }
                    )
                }
                else{

                }

            }


        }

    }
}