package com.example.labirynt

import android.content.Context
import android.graphics.RectF
import org.json.JSONObject

object LevelLoader {

    fun load(context: Context, file: String, w: Int, h: Int): Level {
        val json = context.assets.open(file).bufferedReader().readText()
        val obj = JSONObject(json)

        val scaleX = w / 1000f
        val scaleY = h / 600f

        val walls = mutableListOf<RectF>()
        val wallsJson = obj.getJSONArray("walls")

        for (i in 0 until wallsJson.length()) {
            val o = wallsJson.getJSONObject(i)
            walls.add(
                RectF(
                    o.getDouble("x").toFloat() * scaleX,
                    o.getDouble("y").toFloat() * scaleY,
                    (o.getDouble("x") + o.getDouble("w")).toFloat() * scaleX,
                    (o.getDouble("y") + o.getDouble("h")).toFloat() * scaleY
                )
            )
        }

        return Level(
            obj.getJSONObject("start").getDouble("x").toFloat() * scaleX,
            obj.getJSONObject("start").getDouble("y").toFloat() * scaleY,
            obj.getJSONObject("goal").getDouble("x").toFloat() * scaleX,
            obj.getJSONObject("goal").getDouble("y").toFloat() * scaleY,
            obj.getJSONObject("goal").getDouble("radius").toFloat() * scaleX,
            walls
        )
    }
}
