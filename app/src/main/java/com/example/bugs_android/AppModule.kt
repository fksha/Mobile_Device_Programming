package com.example.bugs_android

import com.example.bugs_android.tournament.TournamentViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        GameViewModel()
    }
    viewModel {
        TournamentViewModel()
    }
}