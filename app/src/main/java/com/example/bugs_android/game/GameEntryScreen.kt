
package com.example.bugs_android.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import com.example.bugs_android.data.UserEntity

@Composable
fun GameEntryScreen(
    users: List<UserEntity>,
    onSelectUser: (UserEntity) -> Unit,
    onRegisterUser: (String) -> Unit,
    onStartGame:(UserEntity) -> Unit,
    onDeleteUser:(UserEntity) -> Unit
) {
    var playerName by remember {
        mutableStateOf("")
    }

    var userToDelete by remember {
        mutableStateOf<UserEntity?>(null)
    }

    var selectedUser by remember {
        mutableStateOf<UserEntity?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Игра «Жуки»")
        Text("Выбери существующего игрока:")

        for (user in users) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        selectedUser = user
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedUser?.id == user.id) {
                            Color(0xFF0108CA)
                        } else {
                            Color(0xFF7077FF)
                        },
                        contentColor = Color.White
                    )
                ) {
                    Text(user.name)
                }

                TextButton(
                    onClick = {
                        userToDelete = user
                    }
                ) {
                    Text("🗑", fontSize = 18.sp)
                }
            }
        }

        if (userToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    userToDelete = null
                },
                title = {
                    Text("Удалить игрока?")
                },
                text = {
                    Text(
                        "Игрок «${userToDelete?.name}» " +
                                "и его результаты будут удалены. Продолжить?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            userToDelete?.let { user ->
                                onDeleteUser(user)

                                if (selectedUser?.id == user.id) {
                                    selectedUser = null
                                }
                            }

                            userToDelete = null
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            userToDelete = null
                        }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }

        Text("Или зарегистрируй нового:")

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Имя игрока") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0108CA),
                focusedLabelColor = Color(0xFF0108CA),
                cursorColor = Color(0xFF0108CA)
            )
        )

        Button(
            onClick = {
                onRegisterUser(playerName.trim())
                playerName = ""
            },
            enabled = playerName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0108CA),
                contentColor = Color.White
            )
        ) {
            Text("Зарегистрироваться")
        }

        Button(
            onClick = {
                selectedUser?.let { user ->
                    onStartGame(user)
                }
            },
            enabled = selectedUser != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0108CA),
                contentColor = Color.White
            )
        ) {
            Text("Начать игру")
        }
    }
}
