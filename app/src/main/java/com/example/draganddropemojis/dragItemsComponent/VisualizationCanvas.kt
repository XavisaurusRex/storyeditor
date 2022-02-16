package com.example.draganddropemojis.dragItemsComponent

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.draganddropemojis.dragItemsComponent.model.CanvasItem

class VisualizationCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val data = mutableListOf<CanvasItem>()
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 16 * resources.displayMetrics.density
        strokeWidth = 2f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 14f), 50f)
    }

    private val dashLineBorders by lazy {
        RectF(
            3f,
            3f,
            width.toFloat() - 3f,
            height.toFloat() - 3f
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /**canvas.drawText(
        "Hola",0, 4, width/2f,height/2f,paint
        )**/
        data.forEach {
            if (it is CanvasItem.Text) {
                it.calculateMeasures()
                it.draw(canvas)
            } else {
                it.draw(canvas)
            }
        }

        canvas.drawRect(dashLineBorders, paint)
    }

    fun addItem(item: CanvasItem) {
        data.add(item)
        invalidate()
    }

    fun clearAll() {
        data.clear()
        invalidate()
    }

    fun extractItemIfIntersect(x: Float, y: Float): CanvasItem? {
        val inverseCopy = Matrix()
        for (i in data.size - 1 downTo 0) {
            val item = data[i]
            inverseCopy.reset()
            item.matrix.invert(inverseCopy)
            val pointsToDiscover = floatArrayOf(x, y)
            inverseCopy.mapPoints(pointsToDiscover)


            if (item.getBounds().contains(pointsToDiscover[0], pointsToDiscover[1])) {
                data.removeAt(i)
                return item
            }
        }
        return null
    }

}