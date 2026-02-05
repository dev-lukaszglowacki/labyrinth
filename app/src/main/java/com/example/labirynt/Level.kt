package com.example.labirynt

import android.graphics.RectF

class Level(
    val map: Array<CharArray>,
    val tileSize: Float,
    val startX: Float,
    val startY: Float,
    val goalX: Float,
    val goalY: Float,
    val goalRadius: Float,
    val offsetX: Float,
    val offsetY: Float
)