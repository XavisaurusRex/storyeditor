package com.example.draganddropemojis

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.draganddropemojis.databinding.ActivityMainBinding
import com.example.draganddropemojis.dragItemsComponent.model.CanvasItem
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {


    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val resources = listOf(
        R.drawable.sticker_coffe_maker,
        R.drawable.sticker_chef_cook_man,
        R.drawable.sticker_ice_bucket_drink_ice,
        R.drawable.sticker_ice_cream,
        R.drawable.sticker_scissor_tool_school,
        R.drawable.sticker_tsunami_waves,
    )

    val textos = listOf(
        "Lorem Ipsum is simply dummy text of the printing and typesetting",
        "Lorem Ipsum",
        "Lorem Ipsum is simply",
        "fwfewfw",
        "-♥️-"
    )

    private val stickersLoaded: HashMap<Int, Bitmap> = hashMapOf()

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnAddIcon.setOnClickListener {
            val drawableRes = resources.random()
            val bitmapSticker = stickersLoaded[drawableRes] ?: run {
                getDrawable(drawableRes)?.toBitmap()?.let {
                    stickersLoaded[drawableRes] = it
                    it
                }
            }

            bitmapSticker?.let {
                val componentWidth = binding.personalizationComponent.width
                val componentHeight = binding.personalizationComponent.height
                binding.personalizationComponent.addSticker(
                    CanvasItem.Sticker(
                        bitmap = it
                    ).apply {
                        setScale(0.5f, 0f, 0f)
                        setTranslation(
                            componentWidth / 2f - this.width / 2f,
                            componentHeight / 2f - this.height / 2f
                        )
                    }
                )
            }
        }
        binding.btnClear.setOnClickListener {
            binding.personalizationComponent.clearAll()
        }

        binding.btnAddText.setOnClickListener {
            binding.personalizationComponent.addTextView(
                CanvasItem.Text(
                    "Hola como estas"
                )
            )

        }

        binding.btnDownload.setOnClickListener {
            val bitmap = binding.personalizationComponent.extractBitmap()
            val sdf = SimpleDateFormat("dd-M-yyyy_hh:mm:ss")
            val currentDate = sdf.format(Date())
            saveFile(context = this, bitmap, "BERSHKATEST_$currentDate.png")
        }
    }

    fun saveFile(context: Context, b: Bitmap, picName: String?) {
        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE)
            b.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: FileNotFoundException) {
            Log.d("XAVIER", "file not found")
            e.printStackTrace()
        } catch (e: IOException) {
            Log.d("XAVIER", "io exception")
            e.printStackTrace()
        } finally {
            fos?.close()
        }
    }
}
