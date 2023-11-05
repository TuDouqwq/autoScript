package com.lun.auto.images

import android.app.Notification
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.lun.auto.NiuApp.Companion.NiuComponentActivity
import com.lun.auto.accessibility.click
import com.lun.auto.accessibility.findOne
import com.lun.auto.accessibility.text
import com.lun.auto.images.mediaprojection.utils.MediaProjectionHelper
import com.lun.auto.images.utils.BitmapUtils.saveBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class NiuImages {
    companion object {
        private fun initNotice() {
            MediaProjectionHelper.getInstance().setNotificationEngine {
                NotificationHelper.getInstance().createSystem()
                    .setOngoing(true) // 常驻通知栏
                    .setDefaults(Notification.DEFAULT_ALL)
                    .build()
            }
        }

        @JvmStatic
        fun requestScreenCapture() {
            initNotice()
            MediaProjectionHelper.getInstance().startService(NiuComponentActivity)
        }

        @JvmStatic
        fun createVirtualDisplay(requestCode: Int, resultCode: Int, data: Intent?, isScreenCaptureEnable: Boolean, isMediaRecorderEnable: Boolean, landscape: Boolean) {
            MediaProjectionHelper.getInstance()
                .createVirtualDisplay(requestCode, resultCode, data, isScreenCaptureEnable, isMediaRecorderEnable, landscape)
        }

        @JvmStatic
        fun stopScreenCapture() {
            MediaProjectionHelper.getInstance().stopService(NiuComponentActivity)
        }

        fun requestCapture() = CoroutineScope(Dispatchers.IO).launch {
            requestScreenCapture()
            var found = false
            while (!found) {
                if (text("立即开始").findOne(100) != null) {
                    text("立即开始").findOne()?.click()
                    found = true
                }
                if (text("允许").findOne(100) != null) {
                    text("允许").findOne()?.click()
                    found = true
                }
            }
        }

        fun captureScreen(path: String? = null): Bitmap {
            val bitmap: Bitmap? = MediaProjectionHelper.getInstance().capture()
            if (path != null && bitmap != null) saveBitmap(bitmap,path)
            return bitmap ?: throw IllegalStateException("截图失败")
        }

        /**
         * 加载图片
         * @param urlOrFilePath 图片路径，可以是云端图片
         * @param useUrl 是否是云端图片
         * **/
        fun loadImage(urlOrFilePath: String, useUrl: Boolean = true): Bitmap? {
            var bitmap: Bitmap? = null

            try {
                if (useUrl) {
                    val url = URL(urlOrFilePath)
                    val stream = url.openStream()
                    bitmap = BitmapFactory.decodeStream(stream)
                    stream.close()
                } else {
                    bitmap = BitmapFactory.decodeFile(urlOrFilePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return if (bitmap != null) {
                // 创建一个新的 Bitmap 对象
                val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                // 将加载的像素数据复制到新的 Bitmap 对象中
                Canvas(newBitmap).drawBitmap(bitmap, 0f, 0f, null)
                // 回收原来的 Bitmap 对象
                bitmap.recycle()
                // 返回新的 Bitmap 对象
                newBitmap
            } else {
                null
            }
        }
    }
}