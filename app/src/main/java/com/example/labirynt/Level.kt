package com.example.labirynt

import android.graphics.RectF

data class Level(
    val startX: Float,
    val startY: Float,
    val goalX: Float,
    val goalY: Float,
    val goalRadius: Float,
    val walls: List<RectF>
)