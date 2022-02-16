package com.example.draganddropemojis.dragItemsComponent.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.withMatrix

sealed class CanvasItem(
    val originalWidth: Float,
    val originalHeight: Float,
) {

    internal val matrix: Matrix = Matrix()

    var width: Float = originalWidth
        get() = field * scale
    var height: Float = originalHeight
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

    abstract fun draw(canvas: Canvas, context: Context? = null)

    abstract fun getBounds(): RectF

    class Sticker(
        private val bitmap: Bitmap
    ) : CanvasItem(bitmap.width.toFloat(), bitmap.height.toFloat()) {


        override fun draw(canvas: Canvas, context: Context?) {
            canvas.drawBitmap(bitmap, matrix, null)
        }

        override fun getBounds(): RectF {
            return RectF(0f, 0f, originalWidth, originalHeight)
        }

    }

    class Text(
        private var text: String
    ) : CanvasItem(0f, 0f) {

        private val paint: Paint = Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 20f
        }

        init {
            calculateMeasures()
            matrix.setTranslate(50f, 50f)
            matrix.postScale(2f, 2f)
            matrix.postRotate(180f, width / 2f * 7f, height / 2f * 7f)
        }

        override fun getBounds(): RectF {
            calculateMeasures()
            return RectF(0f, -height, width, 0f)
        }

        fun calculateMeasures() {
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            height = (bounds.bottom + bounds.height()).toFloat()
            width = (bounds.left + bounds.width()).toFloat()
        }

        fun getTransformedPointsV2(): FloatArray {
            val allPoints = floatArrayOf(
                0f, -height,
                width, -height,
                0f, 0f,
                width, 0f
            )
            matrix.mapPoints(allPoints)
            return allPoints
        }

        override fun draw(canvas: Canvas, context: Context?) {
            canvas.withMatrix(matrix) {
                canvas.drawText(text, 0f, 0f, paint)

                //canvas.drawText(text, 0, text.length, 0f, 0f, paint)
            }
        }
    }
}