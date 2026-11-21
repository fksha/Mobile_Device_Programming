
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
fun TournamentEntryScreen(
    users: List<UserEntity>,
    selectedUserIds: Set<Int>,
    onToggleUser: (UserEntity) -> Unit,
    onRegisterUser: (String) -> Unit,
    onDeleteUser: (UserEntity) -> Unit,
    onStartTournament: (List<UserEntity>) -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Турнир «Жуки»")

        Text("Выбери участников турнира:")

        for (user in users) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onToggleUser(user) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.id in selectedUserIds) {
                            Color(0xFF0108CA)
                        } else {
                            Color(0xFF7077FF)
                        },
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (user.id in selectedUserIds) {
                            "✓ ${user.name}"
                        } else {
                            user.name
                        }
                    )
                }

                TextButton(
                    onClick = { userToDelete = user }
                ) {
                    Text("🗑", fontSize = 18.sp)
                }
            }
        }

        Text("Выбрано участников: ${selectedUserIds.size}")

        Text(
            text = if (selectedUserIds.size < 3) {
                "Для начала турнира необходимо выбрать ещё ${3 - selectedUserIds.size} игрок(а)."
            } else {
                "Можно начинать турнир!"
            }
        )

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
                        "Игрок «${userToDelete?.name}» и его результаты будут удалены. Продолжить?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            userToDelete?.let { user ->
                                onDeleteUser(user)
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

        Text("Или зарегистрируй нового игрока:")

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
                val selectedUsers = users.filter {
                    it.id in selectedUserIds
                }
                onStartTournament(selectedUsers)
            },
            enabled = selectedUserIds.size >= 3 &&
                    users.count { it.id in selectedUserIds } >= 3,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0108CA),
                contentColor = Color.White
            )
        ) {
            Text("Начать турнир")
        }
    }
}
