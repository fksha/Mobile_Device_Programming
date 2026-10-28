package com.example.bugs_android

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        GameViewModel()
    }
}