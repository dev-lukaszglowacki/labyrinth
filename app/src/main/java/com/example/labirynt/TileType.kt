package com.example.labirynt

enum class TileType(val solid: Boolean) {
    EMPTY(false),
    WALL(true),
    START(false),
    GOAL(false);

    companion object {
        fun fromChar(c: Char): TileType =
            when (c) {
                '1' -> WALL
                'S' -> START
                'G' -> GOAL
                else -> EMPTY
            }
    }
}