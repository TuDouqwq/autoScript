package com.lun.auto.utils

import android.graphics.Bitmap
import androidx.lifecycle.lifecycleScope
import com.drake.net.Post
import com.lun.auto.NiuApp
import com.lun.auto.accessibility.await
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class NiuHttpUtils {
    companion object {
        suspend fun upQrcode(url: String,name: String, img: Bitmap): String {
            var results = ""
            NiuApp.NiuComponentActivity.lifecycleScope.launch {
                val stream = ByteArrayOutputStream()
                img.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(name, "image.jpg",
                        byteArray.toRequestBody("image/jpg".toMediaTypeOrNull(), 0, byteArray.size)
                    )
                    .build()

                results = Post<String>(url){
                    body = requestBody
                }.await()
            }.await()
            return results
        }
    }
}