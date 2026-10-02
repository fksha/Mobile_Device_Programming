package com.example.bugs_android.model

import com.example.bugs_android.enumC.BugType

data class Bug (
    val id: Int,
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Int,
    val type: BugType,
    val points: Int,
    val imageResource: Int
)