package com.example.bugs_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.compose.runtime.*
import com.example.bugs_android.data.GameDatabase
import com.example.bugs_android.game.RecordScreen

class RecordsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val database = GameDatabase.getDatabase(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                var scores by remember {
                    mutableStateOf(emptyList<com.example.bugs_android.data.ScoreWithUsers>())
                }

                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    scores = database.scoreDao().getAllScores()
                }

                RecordScreen(scores = scores)
            }
        }
    }
}