package com.example.draganddropemojis.dragItemsComponent

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import com.example.draganddropemojis.R
import com.example.draganddropemojis.databinding.ViewPersonalizationCanvasBinding
import com.example.draganddropemojis.dragItemsComponent.model.CanvasItem

class PersonalizationCanvas @JvmOverloads constructor(
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

    private val stickersLoaded: HashMap<Int, Bitmap> = hashMapOf()

    @SuppressLint("UseCompatLoadingForDrawables")
    fun addSticker(@DrawableRes drawableRes: Int) {
        val bitmapSticker = stickersLoaded[drawableRes] ?: run {
            context.getDrawable(drawableRes)?.toBitmap(width, width)?.let {
                stickersLoaded[drawableRes] = it
                it
            }
        }

        bitmapSticker?.let {
            binding.backgroundCanvas.addItem(
                CanvasItem.Sticker(
                    bitmap = it
                ).apply {
                    setScale(0.5f, 0f, 0f)
                    setTranslation(
                        this@PersonalizationCanvas.width / 2f - this.width / 2f,
                        this@PersonalizationCanvas.height / 2f - this.height / 2f
                    )
                }
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                binding.backgroundCanvas.extractItemIfIntersect(
                    event.x, event.y
                )?.let {
                    binding.backgroundCanvas.invalidate()
                    binding.transformationCanvas.setActiveItem(it)
                    binding.transformationCanvas.invalidate()
                }

            }
            MotionEvent.ACTION_MOVE -> {
                binding.transformationCanvas.onTouchEvent(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                val item = binding.transformationCanvas.removeActiveItem()
                item?.let { binding.backgroundCanvas.addItem(it) }

            }
        }
        return true
    }

    fun clearAll() {
        binding.backgroundCanvas.clearAll()
    }
}