package com.example.draganddropemojis

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    val dragCustomView by lazy {
        findViewById<DragCustomView>(R.id.dragCustomView)
    }
    val btnAddIcon by lazy {
        findViewById<Button>(R.id.btnAddIcon)
    }
    val btnAddText by lazy {
        findViewById<Button>(R.id.btnAddText)
    }

    val btnClear by lazy {
        findViewById<Button>(R.id.btnClear)
    }

    val btnBack by lazy {
        findViewById<Button>(R.id.btnBack)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnAddIcon.setOnClickListener {
            dragCustomView.addSticker(resources.random())
        }

        btnAddText.setOnClickListener {
            dragCustomView.addText(
                textos.random()
            )
        }

        btnClear.setOnClickListener {
            dragCustomView.clearAll()
        }

        btnBack.setOnClickListener {
            dragCustomView.goBack()
        }
//        dragCustomView.addText("HelloWorld")

    }
}
