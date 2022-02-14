package com.example.draganddropemojis

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.draganddropemojis.databinding.ActivityMainBinding


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

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnAddIcon.setOnClickListener {
            binding.personalizationCanvas.addSticker(
                resources.random()
            )
        }
        binding.btnClear.setOnClickListener {
            binding.personalizationCanvas.clearAll()
        }
    }
}
