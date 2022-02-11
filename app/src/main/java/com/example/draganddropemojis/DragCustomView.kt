package com.example.draganddropemojis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Matrix.MSCALE_X
import android.graphics.Matrix.MSKEW_X
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_POINTER_UP
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.core.graphics.values
import java.util.Stack
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


class DragCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val STICKER_DEFAULT_HEIGHT = 100f
        private const val STICKER_DEFAULT_WIDTH = 100f
        private const val INVALID_POINTER_ID = -1
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    private sealed class Item(
        open var x: Float,
        open var y: Float
    ) {

        val matrix: Matrix = Matrix()

        class Sticker(
            x: Float,
            y: Float,
            val bitmap: Bitmap
        ) : Item(x, y) {

            var width: Float = bitmap.width.toFloat()
            var height: Float = bitmap.height.toFloat()
            val scaleTo = 0.3f

            init {
                matrix.postScale(scaleTo, scaleTo)
                matrix.postTranslate(
                    x - ((width * 0.3f) / 2f),
                    y - ((height * 0.3f) / 2f)
                )
            }


            fun getBounds(): RectF {
                val allPoints = floatArrayOf(
                    0f, 0f,
                    width, 0f,
                    0f, width,
                    width, height
                )

                matrix.mapPoints(allPoints)

                // TODO: IMPROVE THIS, I MAKE BOUND BIG RECT AROUNG ALL ITEM CAUSE MATHEMATICS
                var start = allPoints[0]
                var top = allPoints[1]
                var end = allPoints[0]
                var bottom = allPoints[1]

                allPoints.forEachIndexed { index, value ->
                    if (index % 2 == 0) {
                        if (value < start) {
                            start = value
                        } else if (value > end) {
                            end = value
                        }
                    } else {
                        if (value < top) {
                            top = value
                        } else if (value > bottom) {
                            bottom = value
                        }
                    }
                }

                return RectF(
                    start,
                    top,
                    end,
                    bottom
                )
            }
        }

        class Text(
            x: Float,
            y: Float,
            val text: String
        ) : Item(x, y)

    }

    private var startResizeDistance: Float = -1f
    private var startMovementDistance: Pair<Float, Float> = 0f to 0f
    private val interactionsBackStack = Stack<Interaction>()

    private data class Interaction(
        val item: Item,
        val fromX: Float,
        val fromY: Float
    )

    private val data = mutableListOf<Item>()

    private var capturedItem: Item? = null

    private val textPainter by lazy {
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = 16 * resources.displayMetrics.density
        }
    }

    private fun printMatrix(matrix: Matrix) {
        val m = FloatArray(9)
        matrix.getValues(m)
        Log.d("XAVIER", "printMatrix: transX = ${m[Matrix.MTRANS_X]}")
        Log.d("XAVIER", "printMatrix: transY = ${m[Matrix.MTRANS_Y]}")
        Log.d("XAVIER", "printMatrix: scaleX = ${m[MSCALE_X]}")
        Log.d("XAVIER", "printMatrix: scaleY = ${m[Matrix.MSCALE_Y]}")

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        minimumWidth = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minimumWidth, widthMeasureSpec, 1)

        minimumHeight = MeasureSpec.getSize(w) + paddingBottom + paddingTop
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )

        setMeasuredDimension(w, h)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private var activePointerId = INVALID_POINTER_ID
    private var oldRotationDegrees: Float? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount <= 1) {
            startResizeDistance = -1f
            oldRotationDegrees = null
        }
        when (event.action) {
            ACTION_DOWN -> {
                activePointerId = event.getPointerId(0);
                startMovementDistance = event.x to event.y
                captureElement(event.x, event.y)

            }
            ACTION_MOVE -> {
                val pointerIndex: Int = event.findPointerIndex(activePointerId);

                if (pointerIndex == 0) {
                    capturedItem?.let {
                        dragItem(
                            it,
                            event.getX(0) - startMovementDistance.first,
                            event.getY(0) - startMovementDistance.second
                        )
                    }

                    startMovementDistance = event.getX(0) to event.getY(0)
                }
                if (event.pointerCount > 1) {

                    capturedItem?.let {

                        // RESIZE ITEM ZOOM
                        val distanciaActual = getDistanceBetweenTwoPoints(
                            event.getX(0), event.getY(0),
                            event.getX(1), event.getY(1)
                        )

                        if (startResizeDistance == -1f) {
                            startResizeDistance = distanciaActual
                        }

                        resizeItem(
                            it, distanciaActual - startResizeDistance, event.getX(0), event.getY(0)
                        )

                        startResizeDistance = distanciaActual

                        // ROTATE

                        val degrees = getDegreesFromTouchEvent(
                            event.getX(1), event.getY(1)
                        ) * -1

                        if (oldRotationDegrees == null) {
                            oldRotationDegrees = degrees
                        }

                        rotateItem(
                            it, degrees - oldRotationDegrees!!,
                            event.getX(0),
                            event.getY(0)
                        )

                        oldRotationDegrees = degrees
                    }
                }

                if (capturedItem != null) {
                    invalidate()
                }

            }
            ACTION_UP -> {
                activePointerId = INVALID_POINTER_ID
                startMovementDistance = 0f to 0f
                oldRotationDegrees = null
            }
            ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
            }
            ACTION_POINTER_UP -> {
                val pointerIndex: Int =
                    (event.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                val pointerId: Int = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0

                    activePointerId = event.getPointerId(newPointerIndex)
                } else {
                    startMovementDistance = 0f to 0f
                    oldRotationDegrees = null
                }

            }
        }
        return true
    }

    private fun getDegreesFromTouchEvent(x: Float, y: Float): Float {
        val deltaX: Double = x - (width) / 2.0;
        val deltaY: Double = (height) / 2.0 - y;
        val radians: Double = atan2(deltaY, deltaX);
        return Math.toDegrees(radians).toFloat()
    }

    private fun resizeItem(capturedItem: Item, scaleTo: Float, centerX: Float, centerY: Float) {
        when (capturedItem) {
            is Item.Sticker -> {
                capturedItem.matrix.postScale(
                    (scaleTo / 1000f) + 1,
                    (scaleTo / 1000f) + 1,
                    centerX,
                    centerY
                )
                capturedItem.width += scaleTo
                capturedItem.height += scaleTo
            }
            is Item.Text -> {

            }
        }
    }

    private fun rotateItem(
        capturedItem: Item,
        rotationDegrees: Float,
        centerX: Float,
        centerY: Float
    ) {
        when (capturedItem) {
            is Item.Sticker -> {
                capturedItem.matrix.postRotate(
                    rotationDegrees,
                    centerX,
                    centerY
                )
            }
            is Item.Text -> {

            }
        }
    }


    private fun getDistanceBetweenTwoPoints(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    private fun dragItem(capturedItem: Item, destX: Float, destY: Float) {
        (capturedItem as? Item.Sticker)?.let {
            capturedItem.matrix.postTranslate(destX, destY)
        }
    }

    private fun captureElement(touchX: Float, touchY: Float): Boolean {
        capturedItem = data.lastOrNull {
            when (it) {
                is Item.Sticker -> {
                    isTouchOnSticker(it, touchX, touchY)
                }
                is Item.Text -> {
                    isTouchOnText(it, touchX, touchY)

                }
            }
        }?.also {
            interactionsBackStack.push(
                Interaction(
                    it,
                    it.x,
                    it.y
                )
            )
            data.remove(it)
            data.add(it)
        }

        return capturedItem != null
    }

    private fun isTouchOnText(it: Item.Text, touchX: Float, touchY: Float): Boolean {
        val rect = Rect(0, 0, 0, 0)
        textPainter.getTextBounds(it.text, 0, it.text.length, rect)
        return it.x + rect.left <= touchX && it.x + rect.right >= touchX && it.y + rect.top <= touchY && it.y + rect.bottom >= touchY
    }

    private fun isTouchOnSticker(item: Item.Sticker, touchX: Float, touchY: Float): Boolean {
//        Log.d("XAVIER", "isTouchOnSticker: ${item.matrix}")
//        Log.d("XAVIER", " --- X: $touchX  --- Y: $touchY")
        return item.getBounds().contains(touchX, touchY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            data.forEach {
                when (it) {
                    is Item.Sticker -> {
                        canvas.drawBitmap(
                            it.bitmap,
                            it.matrix,
                            textPainter
                        )
                    }
                }
            }

            /**
             * Matrix rotator = new Matrix();

            // rotate around (0,0)
            rotator.postRotate(90);

            // or, rotate around x,y
            // NOTE: coords in bitmap-space!
            int xRotate = ...
            int yRotate = ...
            rotator.postRotate(90, xRotate, yRotate);

            // to set the position in canvas where the bitmap should be drawn to;
            // NOTE: coords in canvas-space!
            int xTranslate = ...
            int yTranslate = ...
            rotator.postTranslate(xTranslate, yTranslate);

            canvas.drawBitmap(bitmap, rotator, paint);
             */
        }
    }

    fun addSticker(decodeResource: Bitmap) {
        Log.d("XAVIER", "NUMBER OF STICKERS ${data.size}")
        data.add(
            Item.Sticker(
                x = width / 2f,
                y = height / 2f,
                bitmap = decodeResource
            )
        )
        invalidate()
    }

    fun addText(text: String) {
        data.add(
            Item.Text(
                x = width / 2f,
                y = height / 2f,
                text = text
            )
        )
        invalidate()
    }

    fun clearAll() {
        data.clear()
        invalidate()
    }

    fun goBack() {
//        if (interactionsBackStack.isNotEmpty()) {
//            val interaction = interactionsBackStack.pop()
//            interaction.item.setXY(interaction.fromX, interaction.fromY)
//            invalidate()
//        }
    }

    private fun Matrix.getRotationAngle() = values().let {
        atan2(
            it[MSKEW_X],
            it[MSCALE_X],
        ) * (180f / Math.PI.toFloat())
    }
}