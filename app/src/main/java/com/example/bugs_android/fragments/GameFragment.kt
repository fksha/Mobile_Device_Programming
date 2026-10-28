
package com.example.bugs_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.example.bugs_android.data.GameDatabase
import com.example.bugs_android.data.UserEntity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.bugs_android.GameViewModel
import com.example.bugs_android.game.GameCanvas
import com.example.bugs_android.game.GameEntryScreen
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameFragment : Fragment() {
    private val gameViewModel: GameViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val database = GameDatabase.getDatabase(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                //GameCanvas()
                var users by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
                //var selectedUsers by remember { mutableStateOf<UserEntity?>(null) }

                //var isGameStarted by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    users = database.userDao().getAllUsers()

                }

                if(!gameViewModel.isGameStarted){
                    GameEntryScreen(
                        users = users,
                        selectedUserId = gameViewModel.selectedUserId,
                        onSelectUser = { user ->
                            gameViewModel.selectUser(user.id)
                        },

                        onRegisterUser = { name ->
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
                        onStartGame = {user ->
                            gameViewModel.startGame(user.id)
                        },
                        onDeleteUser = { user ->
                            coroutineScope.launch {
                                database.userDao().deleteUser(user)

                                users = database.userDao().getAllUsers()

                                Toast.makeText(
                                    requireContext(),
                                    "Игрок удалён",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onSelectUserCleared = {
                            gameViewModel.clearSelectedUser()
                        }

                    )
                }
                else{
                    GameCanvas(userId = gameViewModel.selectedUserId!!,
                        viewModel = gameViewModel,
                        onChangePlayer = {
                            gameViewModel.changePlayer()
                        })
                }

            }

        }
    }
}