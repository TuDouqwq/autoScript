package com.lun.mlkit

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

class NiuMlKit {
    companion object {
        data class TextRegion(val text: String, val rect: Rect)

        private var recognizer: TextRecognizer? = null

        init {
            recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            println("MLKit initialized successfully")
        }

        fun ocr(img: Bitmap): MutableList<TextRegion> {
            val recognizer = recognizer ?: throw IllegalStateException("MlKIT-OCR初始化失败")
            val image = InputImage.fromBitmap(img, 0)
            val textRegions = mutableListOf<TextRegion>()
            try {
                val result = Tasks.await(recognizer.process(image))
                result.textBlocks.forEach { textBlock ->
                    textBlock.boundingBox?.let { boundingBox ->
                        val textRegion = TextRegion(textBlock.text, boundingBox)
                        textRegions.add(textRegion)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return textRegions
        }
    }
}