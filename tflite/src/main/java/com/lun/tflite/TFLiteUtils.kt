package com.lun.tflite

import android.graphics.*
import android.util.Size
import java.util.*
import kotlin.Comparator

class TFLiteUtils {
    companion object {
        private const val DETECT_THRESHOLD = 0.25
        private const val IOU_THRESHOLD = 0.45
        private const val IOU_CLASS_DUPLICATED_THRESHOLD = 0.7

        /* 在bitmap上绘画矩阵 */
        fun drawRectOnBitmap(bitmap: Bitmap, rect: RectF): Bitmap {
            val result = bitmap.copy(bitmap.config, true)
            val canvas = Canvas(result)
            val paint = Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRect(rect, paint)
            return result
        }

        /**
         * 映射坐标到原始的bitmap上
         * @param axis 需要映射原图的坐标
         * @param bitmap 原位图
         * **/
        fun mapPointToBitmap(axis: Point, bitmap: Bitmap, inputSize: Size): Point {
            val letterboxedWidth = inputSize.width
            val letterboxedHeight = inputSize.height
            val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val letterboxedBitmapWidth: Int
            val letterboxedBitmapHeight: Int
            val startX: Int
            val startY: Int

            if (bitmapRatio > letterboxedWidth.toFloat() / letterboxedHeight.toFloat()) {
                letterboxedBitmapWidth = letterboxedWidth
                letterboxedBitmapHeight = (letterboxedWidth.toFloat() / bitmapRatio).toInt()
                startX = 0
                startY = (letterboxedHeight - letterboxedBitmapHeight) / 2
            } else {
                letterboxedBitmapHeight = letterboxedHeight
                letterboxedBitmapWidth = (letterboxedHeight.toFloat() * bitmapRatio).toInt()
                startX = (letterboxedWidth - letterboxedBitmapWidth) / 2
                startY = 0
            }

            val scaleX = bitmap.width.toFloat() / letterboxedBitmapWidth.toFloat()
            val scaleY = bitmap.height.toFloat() / letterboxedBitmapHeight.toFloat()

            val x = (axis.x - startX) * scaleX
            val y = (axis.y - startY) * scaleY

            return Point(x.toInt(), y.toInt())
        }

        /**
         * 映射矩阵到原始的bitmap上
         * @param rect 需要映射原图的矩阵
         * @param bitmap 原位图
         * **/
        fun mapRectToBitmap(rect: Rect, bitmap: Bitmap, inputSize: Size): Rect {
            val letterboxedWidth = inputSize.width
            val letterboxedHeight = inputSize.height
            val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val letterboxedBitmapWidth: Int
            val letterboxedBitmapHeight: Int
            val startX: Int
            val startY: Int

            if (bitmapRatio > letterboxedWidth.toFloat() / letterboxedHeight.toFloat()) {
                letterboxedBitmapWidth = letterboxedWidth
                letterboxedBitmapHeight = (letterboxedWidth.toFloat() / bitmapRatio).toInt()
                startX = 0
                startY = (letterboxedHeight - letterboxedBitmapHeight) / 2
            } else {
                letterboxedBitmapHeight = letterboxedHeight
                letterboxedBitmapWidth = (letterboxedHeight.toFloat() * bitmapRatio).toInt()
                startX = (letterboxedWidth - letterboxedBitmapWidth) / 2
                startY = 0
            }

            val scaleX = bitmap.width.toFloat() / letterboxedBitmapWidth.toFloat()
            val scaleY = bitmap.height.toFloat() / letterboxedBitmapHeight.toFloat()

            val mappedLeft = (rect.left - startX) * scaleX
            val mappedTop = (rect.top - startY) * scaleY
            val mappedRight = (rect.right - startX) * scaleX
            val mappedBottom = (rect.bottom - startY) * scaleY

            return Rect(
                mappedLeft.toInt(),
                mappedTop.toInt(),
                mappedRight.toInt(),
                mappedBottom.toInt()
            )
        }

        /**
         * 实现Letterbox效果
         * @param bitmap 需要实现letterbox效果的位图
         * **/
        fun letterboxBitmap(bitmap: Bitmap, inputSize: Size): Bitmap {
            val width = inputSize.width
            val height = inputSize.height
            val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val requestedRatio = width.toFloat() / height.toFloat()

            val newWidth: Int
            val newHeight: Int
            val startX: Int
            val startY: Int

            if (bitmapRatio > requestedRatio) {
                newWidth = width
                newHeight = (width.toFloat() / bitmapRatio).toInt()
                startX = 0
                startY = (height - newHeight) / 2
            } else {
                newWidth = (height.toFloat() * bitmapRatio).toInt()
                newHeight = height
                startX = (width - newWidth) / 2
                startY = 0
            }

            val letterboxedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(letterboxedBitmap)
            canvas.drawColor(Color.BLACK)
            canvas.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(startX, startY, startX + newWidth, startY + newHeight),
                null
            )

            return letterboxedBitmap
        }

        /**
         * 实现Letterbox效果
         * @param originalBitmap 需要实现letterbox效果的位图
         * **/
        fun createLetterboxedBitmap(originalBitmap: Bitmap, inputSize: Size): Bitmap {
            val targetWidth = inputSize.width
            val targetHeight = inputSize.height

            val originalWidth = originalBitmap.width
            val originalHeight = originalBitmap.height

            val targetAspectRatio = targetWidth.toFloat() / targetHeight
            val originalAspectRatio = originalWidth.toFloat() / originalHeight

            var newWidth = targetWidth
            var newHeight = targetHeight
            var xPosition = 0
            var yPosition = 0

            if (originalAspectRatio > targetAspectRatio) {
                // Image is wider than target, add letterboxing to top and bottom
                newHeight = (targetWidth / originalAspectRatio).toInt()
                yPosition = (targetHeight - newHeight) / 2
            } else {
                // Image is taller than target, add letterboxing to left and right
                newWidth = (targetHeight * originalAspectRatio).toInt()
                xPosition = (targetWidth - newWidth) / 2
            }

            // Create a new Bitmap with the desired dimensions and a black background
            val letterboxedBitmap =
                Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(letterboxedBitmap)
            canvas.drawColor(Color.BLACK)

            // Draw the original Bitmap onto the center of the new Bitmap
            canvas.drawBitmap(
                originalBitmap, Rect(0, 0, originalWidth, originalHeight),
                Rect(xPosition, yPosition, xPosition + newWidth, yPosition + newHeight), null
            )

            return letterboxedBitmap
        }

        /**
         * 实现Letterbox效果
         * @param bitmap 需要实现letterbox效果的位图
         * **/
        fun letterbox(bitmap: Bitmap, inputSize: Size): Bitmap {
            val targetWidth = inputSize.width
            val targetHeight = inputSize.height
            val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()
            val sourceRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            var width = targetWidth
            var height = targetHeight
            var x = 0
            var y = 0

            if (sourceRatio > targetRatio) {
                height = (width / sourceRatio).toInt()
                y = (targetHeight - height) / 2
            } else {
                width = (height * sourceRatio).toInt()
                x = (targetWidth - width) / 2
            }

            val result = Bitmap.createBitmap(targetWidth, targetHeight, bitmap.config)
            val canvas = Canvas(result)
            canvas.drawColor(Color.BLACK)
            canvas.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(x, y, x + width, y + height),
                null
            )
            return result
        }

        fun nms(allRecognitions: ArrayList<Recognition>, outputSize: IntArray): ArrayList<Recognition> {
            val nmsRecognitions = ArrayList<Recognition>()

            // 遍历每个类别, 在每个类别下做nms
            for (i in 0 until outputSize[2] - 5) {
                // 这里为每个类别做一个队列, 把labelScore高的排前面
                val pq = PriorityQueue(
                    6300,
                    Comparator<Recognition> { l, r ->
                        // Intentionally reversed to put high confidence at the head of the queue.
                        r.confidence.compareTo(l.confidence)
                    })

                // 相同类别的过滤出来, 且obj要大于设定的阈值
                for (j in allRecognitions.indices) {
                    if (allRecognitions[j].labelId == i && allRecognitions[j].confidence > DETECT_THRESHOLD) {
                        pq.add(allRecognitions[j])
                    }
                }

                // nms循环遍历
                while (pq.size > 0) {
                    // 概率最大的先拿出来
                    val detections = pq.toTypedArray()
                    val max = detections[0]
                    nmsRecognitions.add(max)
                    pq.clear()

                    for (k in 1 until detections.size) {
                        val detection = detections[k]
                        if (boxIou(
                                max.location,
                                detection.location
                            ) < IOU_THRESHOLD
                        ) {
                            pq.add(detection)
                        }
                    }
                }
            }
            return nmsRecognitions
        }

        /**
         * 对所有数据不区分类别做非极大抑制
         *
         * @param allRecognitions
         * @return
         */
        fun nmsAllClass(allRecognitions: ArrayList<Recognition>): ArrayList<Recognition> {
            val nmsRecognitions = ArrayList<Recognition>()

            val pq = PriorityQueue(
                100,
                Comparator<Recognition> { l, r ->
                    // Intentionally reversed to put high confidence at the head of the queue.
                    r.confidence.compareTo(l.confidence)
                })

            // 相同类别的过滤出来, 且obj要大于设定的阈值
            for (j in allRecognitions.indices) {
                if (allRecognitions[j].confidence > DETECT_THRESHOLD) {
                    pq.add(allRecognitions[j])
                }
            }

            while (pq.size > 0) {
                // 概率最大的先拿出来
                val a = arrayOfNulls<Recognition>(pq.size)
                val detections = pq.toArray(a)
                val max = detections[0]!!
                nmsRecognitions.add(max)
                pq.clear()

                for (k in 1 until detections.size) {
                    val detection = detections[k]!!
                    if (boxIou(
                            max.location,
                            detection.location
                        ) < IOU_CLASS_DUPLICATED_THRESHOLD
                    ) {
                        pq.add(detection)
                    }
                }
            }
            return nmsRecognitions
        }

        private fun boxIou(a: RectF, b: RectF): Float {
            val intersection: Float = boxIntersection(a, b)
            val union: Float = boxUnion(a, b)
            return if (union <= 0) 1F else intersection / union
        }

        private fun boxIntersection(
            a: RectF,
            b: RectF
        ): Float {
            val maxLeft = if (a.left > b.left) a.left else b.left
            val maxTop = if (a.top > b.top) a.top else b.top
            val minRight = if (a.right < b.right) a.right else b.right
            val minBottom = if (a.bottom < b.bottom) a.bottom else b.bottom
            val w = minRight - maxLeft
            val h = minBottom - maxTop
            return if (w < 0 || h < 0) 0F else w * h
        }

        private fun boxUnion(a: RectF, b: RectF): Float {
            val i = boxIntersection(a, b)
            return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i
        }
    }
}