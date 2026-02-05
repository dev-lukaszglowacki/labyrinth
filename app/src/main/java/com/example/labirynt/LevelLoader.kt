package com.example.labirynt

import android.content.Context
import org.json.JSONObject

object LevelLoader {

    fun load(context: Context, file: String, w: Int, h: Int): Level {
        val json = context.assets.open(file).bufferedReader().readText()
        val obj = JSONObject(json)

        val mapJson = obj.getJSONArray("map")
        val rows = mapJson.length()
        val cols = mapJson.getString(0).length

        val baseTile = obj.getInt("tileSize").toFloat()
        val scaleX = w / (cols * baseTile)
        val scaleY = h / (rows * baseTile)
        val scale = minOf(scaleX, scaleY)

        val tileSize = baseTile * scale

        val mapWidth = cols * tileSize
        val mapHeight = rows * tileSize

        val offsetX = (w - mapWidth) / 2f
        val offsetY = (h - mapHeight) / 2f

        val map = Array(rows) { y ->
            mapJson.getString(y).toCharArray()
        }

        var sx = 0f
        var sy = 0f
        var gx = 0f
        var gy = 0f

        for (y in 0 until rows) {
            for (x in 0 until cols) {
                when (map[y][x]) {
                    'S' -> {
                        sx = (x + 0.5f) * tileSize
                        sy = (y + 0.5f) * tileSize
                    }
                    'G' -> {
                        gx = (x + 0.5f) * tileSize
                        gy = (y + 0.5f) * tileSize
                    }
                }
            }
        }

        return Level(
            map,
            tileSize,
            sx + offsetX,
            sy + offsetY,
            gx + offsetX,
            gy + offsetY,
            tileSize * 0.4f,
            offsetX,
            offsetY
        )
    }
}