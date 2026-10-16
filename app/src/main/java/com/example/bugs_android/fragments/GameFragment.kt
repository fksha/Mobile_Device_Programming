
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
import com.example.bugs_android.game.GameCanvas
import com.example.bugs_android.game.GameEntryScreen
import kotlinx.coroutines.launch

class GameFragment : Fragment() {
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
                var selectedUsers by remember { mutableStateOf<UserEntity?>(null) }

                var isGameStarted by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    users = database.userDao().getAllUsers()

                }

                if(!isGameStarted){
                    GameEntryScreen(
                        users = users,
                        onSelectUser = { user ->

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
                            selectedUsers = user
                            isGameStarted = true
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

                    )
                }
                else{
                    GameCanvas(userId = selectedUsers!!.id,
                        onChangePlayer = {
                            selectedUsers = null
                            isGameStarted = false
                        })
                }

            }

        }
    }
}