package com.example.draganddropemojis.dragItemsComponent

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
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


class TransformItemCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    private var previousMovementDistance: PointF? = null
    private var previousPointsDistance: Float? = null
    private var previousRotationDegrees: Float? = null

    private var activeItem: CanvasItem? = null

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 16 * resources.displayMetrics.density
        strokeWidth = 2f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 14f), 50f)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return activeItem?.let {
            if (event.action == ACTION_MOVE) {
                dispatchMovement(event, it)

                if (event.pointerCount > 1) {
                    dispatchScaling(event, it)
                    dispatchRotation(event, it)
                } else {
                    previousPointsDistance = null
                    previousRotationDegrees = null
                }

                invalidate()
                true
            } else super.dispatchTouchEvent(event)
        } ?: super.dispatchTouchEvent(event)
    }

    private fun dispatchMovement(event: MotionEvent, activeItem: CanvasItem) {
        if (previousMovementDistance == null) {
            previousMovementDistance = PointF(event.getX(0), event.getY(0))
        }

        dragItem(
            activeItem,
            event.getX(0) - previousMovementDistance!!.x,
            event.getY(0) - previousMovementDistance!!.y
        )

        previousMovementDistance = PointF(event.getX(0), event.getY(0))
    }

    private fun dispatchScaling(event: MotionEvent, item: CanvasItem) {
        val currentDistance = getDistanceBetweenTwoPoints(
            event.getX(0), event.getY(0),
            event.getX(1), event.getY(1)
        )

        if (previousPointsDistance == null) {
            previousPointsDistance = currentDistance
        }

        val previousScale = Matrix(item.matrix)
        val previousScaleFactor = item.scale

        item.setScale(
            ((currentDistance - previousPointsDistance!!) / 1000f) + 1,
            event.getX(0),
            event.getY(0)
        )

        val transformedPoints = item.getTransformedPoints()

        val maxX = maxOf(
            transformedPoints[0],
            transformedPoints[2],
            transformedPoints[4],
            transformedPoints[6]
        )
        val minX = minOf(
            transformedPoints[0],
            transformedPoints[2],
            transformedPoints[4],
            transformedPoints[6]
        )
        val maxY = minOf(
            transformedPoints[1],
            transformedPoints[3],
            transformedPoints[5],
            transformedPoints[7]
        )
        val minY = minOf(
            transformedPoints[1],
            transformedPoints[3],
            transformedPoints[5],
            transformedPoints[7]
        )

        if (minY < 0f || maxY > height || minX < 0f && maxX > width) {
            item.matrix.set(previousScale)
        } else if (minX < 0f && maxX < width) {
            item.scale = previousScaleFactor
            item.setTranslation(
                -minX,
                0f
            )
        } else if (maxX > width && minX > 0f) {
            item.scale = previousScaleFactor
            item.setTranslation(
                width - maxX,
                0f
            )
        }

        previousPointsDistance = currentDistance
    }

    private fun dispatchRotation(event: MotionEvent, item: CanvasItem) {
        val currentDegrees = getDegreesFromTouchEvent(
            event.getX(1), event.getY(1)
        ) * -1

        if (previousRotationDegrees == null) {
            previousRotationDegrees = currentDegrees
        }
        val previousToRotation = Matrix(item.matrix)
        val previousToRotationDegrees = item.rotation

        rotateItem(
            item,
            currentDegrees - previousRotationDegrees!!,
            event.getX(0),
            event.getY(0)
        )

        if (!isInBounds(item.getTransformedPoints())) {
            item.matrix.set(previousToRotation)
            item.rotation = previousToRotationDegrees
        } else {
            previousRotationDegrees = currentDegrees
        }

    }

    private fun isInBounds(floatArray: FloatArray): Boolean {
        return isInWidth(
            floatArray[0],
            floatArray[2],
            floatArray[4],
            floatArray[6]
        ) && isInHeight(
            floatArray[1],
            floatArray[3],
            floatArray[5],
            floatArray[7]
        )
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (activeItem is CanvasItem.Text) {
            val item = (activeItem as CanvasItem.Text)
            item.calculateMeasures()
            item.draw(canvas)
            (activeItem as CanvasItem.Text).getTransformedPointsV2().also { points ->
                canvas.drawLine(points[0], points[1], points[2], points[3], paint)
                canvas.drawLine(points[2], points[3], points[6], points[7], paint)
                canvas.drawLine(points[6], points[7], points[4], points[5], paint)
                canvas.drawLine(points[4], points[5], points[0], points[1], paint)
            }
        } else {
            activeItem?.draw(canvas)
            activeItem?.getTransformedPoints()?.also { points ->
                canvas.drawLine(points[0], points[1], points[2], points[3], paint)
                canvas.drawLine(points[2], points[3], points[6], points[7], paint)
                canvas.drawLine(points[6], points[7], points[4], points[5], paint)
                canvas.drawLine(points[4], points[5], points[0], points[1], paint)
            }
        }
    }

    fun setActiveItem(it: CanvasItem) {
        activeItem = it
        previousMovementDistance = null
        previousPointsDistance = null
        previousRotationDegrees = null
        invalidate()
    }

    fun removeActiveItem(): CanvasItem? {
        return activeItem.also {
            activeItem = null
            previousMovementDistance = null
            previousPointsDistance = null
            previousRotationDegrees = null
            invalidate()
        }
    }

//    fun animateEnteringTransition() {
//        if (!valueAnimator.isRunning && !valueAnimator.isStarted) {
//            var reversed = false
//            valueAnimator.apply {
//                duration = 2000
//                repeatMode = ValueAnimator.RESTART
//                interpolator = LinearInterpolator()
//            }.addUpdateListener { valueAnimator ->
//                val value = valueAnimator.animatedValue as Float
//                Log.d("XAVIER", "ANIMATION VALUE $value")
//                activeItem?.let {
//                    val f = FloatArray(9)
//                    it.matrix.getValues(f)
//                    val scaleX: Float = f[Matrix.MSCALE_X]
//                    var scaleY: Float = f[Matrix.MSCALE_Y]
//                    Log.d("XAVIER", "VERDADERA ESCALA $scaleX $scaleY")
//
//                    val centerXY = floatArrayOf(it.originalWidth / 2f, it.originalHeight / 2f)
//                    it.matrix.mapPoints(centerXY)
//
//                    it.setScale(
//                        1f + if (reversed) +value else -value,
//                        centerXY[0],
//                        centerXY[1]
//                    )
//                    invalidate()
//                } ?: valueAnimator.cancel()
//            }
//
//            valueAnimator.start()
//            valueAnimator.doOnEnd {
//                if(!reversed){
//                reversed = true
//                valueAnimator.reverse()
//                }
//            }
//        }
//    }
}