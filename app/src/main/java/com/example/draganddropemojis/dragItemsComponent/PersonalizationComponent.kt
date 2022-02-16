package com.example.draganddropemojis.dragItemsComponent

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import com.example.draganddropemojis.R
import com.example.draganddropemojis.databinding.ViewPersonalizationCanvasBinding
import com.example.draganddropemojis.dragItemsComponent.model.CanvasItem
import kotlin.math.abs

class PersonalizationComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewPersonalizationCanvasBinding = ViewPersonalizationCanvasBinding.bind(
        inflate(
            getContext(),
            R.layout.view_personalization_canvas,
            this
        )
    )


    fun addSticker(item: CanvasItem.Sticker) {
        binding.backgroundCanvas.addItem(item)
    }

    fun addTextView(item: CanvasItem.Text) {
        binding.backgroundCanvas.addItem(item)
    }

    private var thereIsSomeTransformation = false
    private var initialX = 0f
    private var initialY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                thereIsSomeTransformation = false
                initialX = event.x
                initialY = event.y
                binding.backgroundCanvas.extractItemIfIntersect(
                    event.x, event.y
                )?.let {
                    binding.backgroundCanvas.invalidate()
                    binding.transformationCanvas.setActiveItem(it)
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                thereIsSomeTransformation =
                    !thereIsSomeTransformation && (event.pointerCount > 1 || abs(initialX - event.x) > 10f || abs(
                        initialY - event.y
                    ) > 10f)
                binding.transformationCanvas.dispatchTouchEvent(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                binding.transformationCanvas.removeActiveItem()?.also {
                    binding.backgroundCanvas.addItem(it)
                }
                if (!thereIsSomeTransformation) {
                    val timeElapsed = event.eventTime - event.downTime
                    if (timeElapsed in 50..400) {

                        Log.d("XAVIER", "PERFORM CLICK")
                        performClick()
                    }
                }
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    fun clearAll() {
        binding.backgroundCanvas.clearAll()
    }

    fun extractBitmap(): Bitmap {
        return binding.backgroundCanvas.drawToBitmap()
    }
}