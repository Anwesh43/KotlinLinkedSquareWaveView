package com.anwesh.uiprojects.linkedsquarewaveview

/**
 * Created by anweshmishra on 04/07/18.
 */

import android.app.Activity
import android.view.View
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val SWV_NODES : Int = 5

class LinkedSquareWaveView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class SWVState(var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += 0.1f * dir
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {

                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class SWAnimator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SWNode (var i : Int, val state : SWVState = SWVState()) {

        var next : SWNode? = null

        var prev : SWNode? = null

        fun update(stopcb : (Int, Float) -> Unit) {
            state.update {
                stopcb(i, it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SWNode {
            var curr : SWNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < SWV_NODES - 1) {
                next = SWNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            prev?.draw(canvas, paint)
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = (0.9f * w) / SWV_NODES
            val index : Int = i % 2
            val currH : Float = gap * index
            val hLine : Float = gap * (1 - 2 * index) * state.scales[1]
            val currX : Float = gap * state.scales[0]
            paint.color = Color.parseColor("#4CAF50")
            paint.strokeWidth = Math.min(w, h) / 50
            paint.strokeCap = Paint.Cap.ROUND
            canvas.save()
            canvas.translate(0.05f * w + i * gap, 0.05f * h + currH)
            canvas.drawLine(0f, 0f, currX, 0f, paint)
            canvas.drawLine(currX, 0f, currX, hLine, paint)
            canvas.restore()
        }
    }

    data class LinkedSquareWave(var i : Int) {

        private var curr : SWNode = SWNode(0)

        private var dir : Int = 1

        fun update(stopcb : (Int, Float) -> Unit) {
            curr.update {j, scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(j, scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }
    }

    data class Renderer(var view : LinkedSquareWaveView) {

        val lsw : LinkedSquareWave = LinkedSquareWave(0)

        val animator : SWAnimator = SWAnimator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            lsw.draw(canvas, paint)
            animator.animate {
                lsw.update {j, scale ->
                    animator.stop()
                    when(scale) {
                        1f -> {

                        }
                    }
                }
            }
        }

        fun handleTap() {
            lsw.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : LinkedSquareWaveView {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val view : LinkedSquareWaveView = LinkedSquareWaveView(activity)
            activity.setContentView(view)
            return view
        }
    }
}