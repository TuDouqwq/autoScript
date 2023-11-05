package com.lun.opencv

import android.graphics.Bitmap
import kotlinx.coroutines.delay
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc

class NiuOpenCV {
    companion object {
        init {
            OpenCVLoader.initDebug()
            println("OpenCV initialized successfully")
        }

        /**
         * 在指定的图像中查找指定的模板，并返回模板在图像中的位置
         *
         * @param templateBitmap 要查找的模板图像
         * @param imageBitmap    要在其中查找模板的图像
         * @param threshold      匹配阈值，取值范围为0~1，表示匹配程度的最小值
         * @param region         在要查找的模板图像中的指定范围
         * @param isGrayscale    是否开启灰度化
         * @return 包含找到的模板位置的矩形，如果未找到或匹配程度不够高，则返回null
         */
        suspend fun findImage(
            templateBitmap: Bitmap?,
            imageBitmap: Bitmap?,
            threshold: Double,
            region: Rect? = null,
            isGrayscale: Boolean = true
        ): android.graphics.Rect? {
            // 判断参数是否为空
            if (templateBitmap == null || imageBitmap == null) return null

            // 将 Bitmaps 转换为 Mats
            val template = Mat()
            val image = Mat()
            Utils.bitmapToMat(templateBitmap, template)
            Utils.bitmapToMat(imageBitmap, image)

            // 如果指定了查找范围，则截取指定区域的图像
            region?.let {
                if (it.width <= templateBitmap.width && it.height <= templateBitmap.height) {
                    val roi = Mat(template, it)
                    roi.copyTo(template)
                }
            }

            if (isGrayscale) {
                // 将图像和模板转换为灰度图像
                Imgproc.cvtColor(template, template, Imgproc.COLOR_BGR2GRAY)
                Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
            }

            // 使用模板匹配算法进行匹配
            val resultMat = Mat()
            /*
            TM_CCOEFF_NORMED 该算法对旋转和缩放有一定的容忍度，并且可以处理图像的部分遮挡和局部变形
            TM_CCORR_NORMED 算法可以适用于更多的背景条件，而且对于图像的部分遮挡和局部变形也有一定的容忍度。但是它可能对旋转和缩放不太敏感，需要对模板进行多个尺度的匹配
             */
            Imgproc.matchTemplate(template, image, resultMat, Imgproc.TM_CCOEFF_NORMED)
            // 获取匹配结果中的最大值和位置
            val mmr = Core.minMaxLoc(resultMat)
            delay(1)
            // 判断匹配结果是否满足阈值
            return if (mmr.maxVal >= threshold) {
                val x = mmr.maxLoc.x.toInt()
                val y = mmr.maxLoc.y.toInt()
                android.graphics.Rect(x, y, (x + imageBitmap.width), (y + imageBitmap.height))
            } else {
                null
            }
        }
    }
}