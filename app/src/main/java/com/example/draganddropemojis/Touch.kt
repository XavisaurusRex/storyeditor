package com.example.draganddropemojis

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import kotlin.math.sqrt


class Touch : OnTouchListener {
    var mode = NONE

    // Remember some things for zooming
    var start = PointF()
    var mid = PointF()
    var oldDist = 1f
    var width = 0
    var height = 0
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        Log.d("Touch", "init onTouch: ")
        val view: ImageView = v as ImageView
        val bounds: Rect = view.drawable.bounds
        width = bounds.right - bounds.left
        height = bounds.bottom - bounds.top
        Log.d("XAVIER", "width: $width height: $height ")
        // Dump touch event to log
        dumpEvent(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("XAVIER", "ACTION_DOWN x: ${event.x} y: ${event.y}")
                savedMatrix.set(matrix)
                start[event.x] = event.y
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.d("XAVIER", "ACTION_POINTER_DOWN x: ${event.x} y: ${event.y}")
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                Log.d("XAVIER", "ACTION_POINTER_UP")
                mode = NONE
            }
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                Log.d("XAVIER", "ACTION_MOVE x: ${event.x} y: ${event.y}")
                matrix.set(savedMatrix)
                matrix.postTranslate(event.x - start.x, event.y - start.y)
            } else if (mode == ZOOM) {
                Log.d("XAVIER", "ACTION_MOVE zoom x: ${event.x} y: ${event.y}")
                val newDist = spacing(event)
                if (newDist > 10f) {
                    matrix.set(savedMatrix)
                    val scale = newDist / oldDist
                    matrix.postScale(scale, scale, mid.x, mid.y)
                }
            }
        }
        //----------------------------------------------------
        limitZoom(matrix)
        limitDrag(matrix)
        //----------------------------------------------------
        Log.d("XAVIER", "SET IMAGE MATRIX: $matrix")
        view.translationX = event.x
        view.translationY = event.y
        return true // indicate event was handled
    }

    /** Show an event in the LogCat view, for debugging  */
    private fun dumpEvent(event: MotionEvent) {
        val names = arrayOf(
            "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
            "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"
        )
        val sb = StringBuilder()
        val action = event.action
        val actionCode = action and MotionEvent.ACTION_MASK
        sb.append("event ACTION_").append(names[actionCode])
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
            || actionCode == MotionEvent.ACTION_POINTER_UP
        ) {
            sb.append("(pid ").append(
                action shr MotionEvent.ACTION_POINTER_ID_SHIFT
            )
            sb.append(")")
        }
        sb.append("[")
        for (i in 0 until event.pointerCount) {
            sb.append("#").append(i)
            sb.append("(pid ").append(event.getPointerId(i))
            sb.append(")=").append(event.getX(i).toInt())
            sb.append(",").append(event.getY(i).toInt())
            if (i + 1 < event.pointerCount) sb.append(";")
        }
        sb.append("]")
//        Log.d("XAVIER", sb.toString())
    }

    /** Determine the space between the first two fingers  */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    /** Calculate the mid point of the first two fingers  */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    private fun limitZoom(m: Matrix) {
        val values = FloatArray(9)
        m.getValues(values)
        var scaleX = values[Matrix.MSCALE_X]
        var scaleY = values[Matrix.MSCALE_Y]
        if (scaleX > MAX_ZOOM) {
            scaleX = MAX_ZOOM
        } else if (scaleX < MIN_ZOOM) {
            scaleX = MIN_ZOOM
        }
        if (scaleY > MAX_ZOOM) {
            scaleY = MAX_ZOOM
        } else if (scaleY < MIN_ZOOM) {
            scaleY = MIN_ZOOM
        }
        values[Matrix.MSCALE_X] = scaleX
        values[Matrix.MSCALE_Y] = scaleY
        m.setValues(values)
    }

    private fun limitDrag(m: Matrix) {
        val values = FloatArray(9)
        m.getValues(values)
        var transX = values[Matrix.MTRANS_X]
        var transY = values[Matrix.MTRANS_Y]
        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        //--- limit moving to left ---
        val minX = (-width + 0) * (scaleX - 1)
        val minY = (-height + 0) * (scaleY - 1)
        //--- limit moving to right ---
        val maxX = minX + width * (scaleX - 1)
        val maxY = minY + height * (scaleY - 1)
        if (transX > maxX) {
            transX = maxX
        }
        if (transX < minX) {
            transX = minX
        }
        if (transY > maxY) {
            transY = maxY
        }
        if (transY < minY) {
            transY = minY
        }
        values[Matrix.MTRANS_X] = transX
        values[Matrix.MTRANS_Y] = transY
        m.setValues(values)
    }

    companion object {
        // These matrices will be used to move and zoom image
        var matrix: Matrix = Matrix()
        var savedMatrix: Matrix = Matrix()

        // We can be in one of these 3 states
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
        private const val MAX_ZOOM = 3.toFloat()
        private const val MIN_ZOOM = 1f
    }
}