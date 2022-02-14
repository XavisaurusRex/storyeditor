package com.example.draganddropemojis.dragItemsComponent

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import com.example.draganddropemojis.dragItemsComponent.model.CanvasItem
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


class TransformationCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    private var previousMovementDistance: PointF? = null
    private var previousResizeDistance: Float? = null
    private var previousRotationDegrees: Float? = null

    private var capturedItem: CanvasItem? = null

    private var activePointerId = INVALID_POINTER_ID

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 16 * resources.displayMetrics.density
        strokeWidth = 2f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 14f), 50f)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount <= 1) {
            previousResizeDistance = null
            previousRotationDegrees = null
        }
        return when (event.action) {
            ACTION_MOVE -> {
                capturedItem?.let {

                    if (previousMovementDistance == null) {
                        previousMovementDistance = PointF(event.getX(0), event.getY(0))
                    }

                    dragItem(
                        it,
                        event.getX(0) - previousMovementDistance!!.x,
                        event.getY(0) - previousMovementDistance!!.y
                    )

                    previousMovementDistance = PointF(event.getX(0), event.getY(0))
                }
                if (event.pointerCount > 1) {

                    capturedItem?.let {

                        // RESIZE ITEM ZOOM
                        val distanciaActual = getDistanceBetweenTwoPoints(
                            event.getX(0), event.getY(0),
                            event.getX(1), event.getY(1)
                        )

                        if (previousResizeDistance == null) {
                            previousResizeDistance = distanciaActual
                        }

                        resizeItem(
                            it,
                            distanciaActual - previousResizeDistance!!,
                            event.getX(0),
                            event.getY(0)
                        )

                        previousResizeDistance = distanciaActual

                        // ROTATE

                        val degrees = getDegreesFromTouchEvent(
                            event.getX(1), event.getY(1)
                        ) * -1

                        if (previousRotationDegrees == null) {
                            previousRotationDegrees = degrees
                        }

                        rotateItem(
                            it, degrees - previousRotationDegrees!!,
                            event.getX(0),
                            event.getY(0)
                        )

                        previousRotationDegrees = degrees
                    }
                }

                if (capturedItem != null) {
                    invalidate()
                }
                true
            }
            else -> false
        }
    }

    private fun getDegreesFromTouchEvent(x: Float, y: Float): Float {
        val deltaX: Double = x - (width) / 2.0;
        val deltaY: Double = (height) / 2.0 - y;
        val radians: Double = atan2(deltaY, deltaX);
        return Math.toDegrees(radians).toFloat()
    }

    private fun resizeItem(
        capturedItem: CanvasItem,
        scaleTo: Float,
        centerX: Float,
        centerY: Float
    ) {
        capturedItem.setScale((scaleTo / 1000f) + 1, centerX, centerY)
    }

    private fun rotateItem(
        capturedItem: CanvasItem,
        rotationDegrees: Float,
        centerX: Float,
        centerY: Float
    ) {
        capturedItem.setRotation(rotationDegrees, centerX, centerY)
    }


    private fun getDistanceBetweenTwoPoints(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    private fun dragItem(capturedItem: CanvasItem, destX: Float, destY: Float) {

        // TODO: I HAVE TO IMPROVE THAT HANDLING ALL TRANSFORMATIONS IN ONE MATRIX THAT APPLY AT
        //  FINAL OF EVENT, TO AVOID TOO MUCH ROLLBACKS ON TRANSFORMATIONS
        capturedItem.setTranslation(
            destX,
            destY
        )

        val pointsTranslated = capturedItem.getTransformedPoints()

        val xAxisInBounds = isInWidth(
            pointsTranslated[0],
            pointsTranslated[2],
            pointsTranslated[4],
            pointsTranslated[6]
        )

        val yAxisInBounds = isInHeight(
            pointsTranslated[1],
            pointsTranslated[3],
            pointsTranslated[5],
            pointsTranslated[7]
        )

        if (xAxisInBounds && !yAxisInBounds) {
            capturedItem.setTranslation(
                -0f,
                -destY
            )
        } else if (!xAxisInBounds && yAxisInBounds) {
            capturedItem.setTranslation(
                -destX,
                0f
            )
        } else if (!xAxisInBounds && !yAxisInBounds) {
            capturedItem.setTranslation(
                -destX,
                -destY
            )
        }

    }

    private fun isInWidth(vararg xs: Float): Boolean {
        return xs.all { it > 0f && it < width }
    }

    private fun isInHeight(vararg ys: Float): Boolean {
        return ys.all { it > 0f && it < height }
    }

    //    private fun captureElement(touchX: Float, touchY: Float): Boolean {
//        capturedItem = data.lastOrNull {
//            when (it) {
//                is Item.Sticker -> {
//                    isTouchOnSticker(it, touchX, touchY)
//                }
//                is Item.Text -> {
//                    isTouchOnText(it, touchX, touchY)
//
//                }
//            }
//        }?.also {
//            interactionsBackStack.push(
//                Interaction(
//                    it,
//                    it.x,
//                    it.y
//                )
//            )
//            data.remove(it)
//            data.add(it)
//        }
//
//        return capturedItem != null
//    }

//    private fun isTouchOnText(it: Item.Text, touchX: Float, touchY: Float): Boolean {
//        val rect = Rect(0, 0, 0, 0)
//        textPainter.getTextBounds(it.text, 0, it.text.length, rect)
//        return it.x + rect.left <= touchX && it.x + rect.right >= touchX && it.y + rect.top <= touchY && it.y + rect.bottom >= touchY
//    }
//
//    private fun isTouchOnSticker(item: Item.Sticker, touchX: Float, touchY: Float): Boolean {
////        Log.d("XAVIER", "isTouchOnSticker: ${item.matrix}")
////        Log.d("XAVIER", " --- X: $touchX  --- Y: $touchY")
//
//        val inverseCopy = Matrix()
//        if (item.matrix.invert(inverseCopy)) {
//            val pointsToDiscover = floatArrayOf(touchX, touchY)
//            inverseCopy.mapPoints(pointsToDiscover)
//            return pointsToDiscover[0] > 0f &&
//                    pointsToDiscover[0] < item.width &&
//                    pointsToDiscover[1] > 0f &&
//                    pointsToDiscover[1] < item.height
//        }
//        return false
//
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        capturedItem?.draw(canvas, paint)
        canvas.drawLine(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    fun setActiveItem(it: CanvasItem) {
        capturedItem = it
    }

    fun removeActiveItem(): CanvasItem? {
        return capturedItem.also {
            capturedItem = null
            previousMovementDistance = null
            previousResizeDistance = null
            previousRotationDegrees = null
        }
    }
}