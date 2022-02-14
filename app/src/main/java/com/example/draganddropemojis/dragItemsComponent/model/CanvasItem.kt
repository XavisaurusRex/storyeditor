package com.example.draganddropemojis.dragItemsComponent.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF

sealed class CanvasItem(
    val originalWidth: Float,
    val originalHeight: Float,
) {

    internal val matrix: Matrix = Matrix()

    val width: Float = originalWidth
        get() = field * scale
    val height: Float = originalHeight
        get() = field * scale

    var scale: Float = 1f
    var translation = PointF(0f, 0f)
    var rotation: Float = 0f


    fun setTranslation(x: Float, y: Float) {
        matrix.postTranslate(x, y)
        translation = PointF(x, y)
    }

    fun setScale(scale: Float, x: Float, y: Float) {
        matrix.postScale(scale, scale, x, y)
        this.scale = scale
    }

    fun setRotation(degrees: Float, x: Float, y: Float) {
        matrix.postRotate(degrees, x, y)
        rotation = degrees
    }

    fun getTransformedPoints(): FloatArray {
        val allPoints = floatArrayOf(
            0f, 0f,
            originalWidth, 0f,
            0f, originalHeight,
            originalWidth, originalHeight
        )
        matrix.mapPoints(allPoints)
        return allPoints
    }

    abstract fun draw(canvas: Canvas, paint: Paint? = null)

    class Sticker(
        private val bitmap: Bitmap
    ) : CanvasItem(bitmap.width.toFloat(), bitmap.height.toFloat()) {


        override fun draw(canvas: Canvas, paint: Paint?) {
            canvas.drawBitmap(bitmap, matrix, null)

            // TODO: ESTO SE DEBE ELIMINAR EN UN FUTURO, ahora existe para testear los clicks y on touch events
            paint?.let {
                getTransformedPoints().also { points ->
                    canvas.drawLine(points[0], points[1], points[2], points[3], it)
                    canvas.drawLine(points[2], points[3], points[6], points[7], paint)
                    canvas.drawLine(points[6], points[7], points[4], points[5], paint)
                    canvas.drawLine(points[4], points[5], points[0], points[1], paint)
                }
            }
        }



    }
}