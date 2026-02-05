package com.example.labirynt

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.*
import android.view.MotionEvent
import android.view.SurfaceView
import kotlin.math.hypot

class GameView(context: Context) : SurfaceView(context), Runnable {

    enum class State { MENU, PLAYING, PAUSED, WIN }

    private var state = State.MENU
    private var thread: Thread? = null
    @Volatile private var running = false

    private val settingsRect = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private var level: Level? = null

    private val radiusBase = 20f
    private var radius = radiusBase
    private var ballX = 0f
    private var ballY = 0f
    private var velX = 0f
    private var velY = 0f

    private var tiltX = 0f
    private var tiltY = 0f

    fun setTilt(pitch: Float, roll: Float) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val swap = prefs.getBoolean("swap_axes", false)

        if (swap) {
            tiltX = -roll
            tiltY = -pitch
        } else {
            tiltX = roll
            tiltY = pitch
        }
    }

    fun start() {
        if (running) return
        running = true
        thread = Thread(this)
        thread?.start()
    }

    fun stop() {
        running = false
        thread = null
    }

    override fun run() {
        while (running) {
            update()
            drawGame()
            Thread.sleep(16)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        level = LevelLoader.load(context, "level1.json", w, h)
        reset()
        state = State.MENU
    }

    private fun reset() {
        level?.let {
            ballX = it.startX
            ballY = it.startY
        }
        velX = 0f
        velY = 0f
        radius = radiusBase
    }

    private fun update() {
        if (state != State.PLAYING) return
        val lvl = level ?: return

        velX += -tiltX * 0.2f
        velY += tiltY * 0.2f
        velX *= 0.98f
        velY *= 0.98f

        val nextX = ballX + velX
        val nextY = ballY + velY

        val nextRect = RectF(
            nextX - radius, nextY - radius,
            nextX + radius, nextY + radius
        )

        var collision = false
        collision = collidesWithMap(lvl, nextRect)

        if (!collision) {
            ballX = nextX
            ballY = nextY
        } else {
            velX *= -1f
            velY *= -1f
        }

        if (hypot(ballX - lvl.goalX, ballY - lvl.goalY) < lvl.goalRadius) {
            state = State.WIN
            postDelayed({ state = State.MENU }, 800)
        }

        handleBorders()
    }

    private fun handleBorders() {
        ballX = ballX.coerceIn(radius, width - radius)
        ballY = ballY.coerceIn(radius, height - radius)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        if (state == State.MENU && settingsRect.contains(event.x, event.y)) {
            context.startActivity(
                Intent(context, SettingsActivity::class.java)
            )
            return true
        }

        when (state) {
            State.MENU -> {
                reset()
                state = State.PLAYING
            }
            State.PLAYING -> state = State.PAUSED
            State.PAUSED -> state = State.PLAYING
            State.WIN -> state = State.MENU
        }
        return true
    }

    private fun vibrate(ms: Long) {
        if (Build.VERSION.SDK_INT >= 26)
            vibrator.vibrate(VibrationEffect.createOneShot(ms, 80))
        else vibrator.vibrate(ms)
    }

    private fun drawGame() {
        val lvl = level ?: return
        if (!holder.surface.isValid) return

        val canvas = holder.lockCanvas()
        canvas.drawColor(Color.WHITE)

        paint.color = Color.DKGRAY
        paint.color = Color.DKGRAY
        for (y in lvl.map.indices) {
            for (x in lvl.map[0].indices) {
                if (lvl.map[y][x] == '1') {
                    canvas.drawRect(
                        x * lvl.tileSize,
                        y * lvl.tileSize,
                        (x + 1) * lvl.tileSize,
                        (y + 1) * lvl.tileSize,
                        paint
                    )
                }
            }
        }

        paint.color = Color.BLACK
        canvas.drawCircle(lvl.goalX, lvl.goalY, lvl.goalRadius, paint)

        if (state != State.MENU && state != State.WIN) {
            paint.color = Color.BLUE
            canvas.drawCircle(ballX, ballY, radius, paint)
        }

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 60f
        paint.color = Color.BLACK

        if (state == State.MENU) {
            val size = 100f
            val pad = 30f

            settingsRect.set(
                width - size - pad,
                pad,
                width - pad,
                pad + size
            )

            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 70f
            paint.color = Color.BLACK

            canvas.drawText("⚙", settingsRect.centerX(), settingsRect.centerY() + 25f, paint)
        }

        when (state) {
            State.MENU -> canvas.drawText("Touch to start", width / 2f, height / 2f, paint)
            State.PAUSED -> canvas.drawText("Pause", width / 2f, height / 2f, paint)
            State.WIN -> canvas.drawText("WIN!", width / 2f, height / 2f, paint)
            else -> {}
        }

        holder.unlockCanvasAndPost(canvas)
    }

    private fun collidesWithMap(level: Level, player: RectF): Boolean {
        val ts = level.tileSize

        val left = (player.left / ts).toInt()
        val right = (player.right / ts).toInt()
        val top = (player.top / ts).toInt()
        val bottom = (player.bottom / ts).toInt()

        for (y in top..bottom) {
            for (x in left..right) {
                if (y !in level.map.indices ||
                    x !in level.map[0].indices
                ) continue

                val tile = TileType.fromChar(level.map[y][x])
                if (!tile.solid) continue

                val tileRect = RectF(
                    x * ts,
                    y * ts,
                    (x + 1) * ts,
                    (y + 1) * ts
                )

                if (RectF.intersects(player, tileRect)) {
                    vibrate(10)
                    return true
                }
            }
        }
        return false
    }
}
