
package com.example.bugs_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bugs_android.data.GameDatabase
import com.example.bugs_android.data.TournamentResultEntity
import com.example.bugs_android.data.TournamentSummary
import com.example.bugs_android.tournament.TournamentResultsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TournamentResultsFragment : Fragment() {

    private val database by lazy {
        GameDatabase.getDatabase(requireContext())
    }

    private var tournaments by mutableStateOf(emptyList<TournamentSummary>())

    private var selectedTournamentId by mutableStateOf<String?>(null)

    private var results by mutableStateOf(emptyList<TournamentResultEntity>())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                TournamentResultsScreen(
                    tournaments = tournaments,
                    selectedTournamentId = selectedTournamentId,
                    results = results,
                    onTournamentClick = { tournamentId ->

                        selectedTournamentId = tournamentId

                        viewLifecycleOwner.lifecycleScope.launch {
                            results = withContext(Dispatchers.IO) {
                                database.tournamentResultDao()
                                    .getResultsForTournament(tournamentId)
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewLifecycleOwner.lifecycleScope.launch {
            tournaments = withContext(Dispatchers.IO) {
                database.tournamentResultDao().getTournamentHistory()
            }

            // Если турнир уже был выбран, обновляем и его результаты
            selectedTournamentId?.let { tournamentId ->
                results = withContext(Dispatchers.IO) {
                    database.tournamentResultDao()
                        .getResultsForTournament(tournamentId)
                }
            }
        }
    }
}
