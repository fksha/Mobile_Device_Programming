package com.example.bugs_android.game


import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

private const val LOGICAL_WIDTH = 1000f
private const val LOGICAL_HEIGHT = 1000f
private const val TOP_PANEL_HEIGHT = 130f

fun createRandomTarget( bugSize: Int): Offset {
    val maxX = (LOGICAL_WIDTH - bugSize).coerceAtLeast(0f)
    val maxY = (LOGICAL_HEIGHT - TOP_PANEL_HEIGHT - bugSize).coerceAtLeast(0f)

    return Offset(
        x = Random.nextFloat() * maxX,
        y = TOP_PANEL_HEIGHT + Random.nextFloat() * maxY
    )
}