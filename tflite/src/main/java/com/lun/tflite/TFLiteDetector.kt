package com.lun.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import com.lun.tflite.TFLiteUtils.Companion.letterbox
import com.lun.tflite.TFLiteUtils.Companion.mapRectToBitmap
import com.lun.tflite.TFLiteUtils.Companion.nms
import com.lun.tflite.TFLiteUtils.Companion.nmsAllClass
import kotlinx.coroutines.delay
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.common.ops.QuantizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.metadata.MetadataExtractor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.max
import kotlin.math.min

class TFLiteDetector {
    companion object {
        private var USE_INT8 = false

        private var inputSize: Size? = null
        private var inputINT8Params: MetadataExtractor.QuantizationParams? = null
        private var outputSize: IntArray? = null
        private var outputINT8Params: MetadataExtractor.QuantizationParams? = null

        private var tflite: Interpreter? = null
        private var options = Interpreter.Options()
        private var associatedAxisLabels: List<String>? = null

        fun loadTFlite(model: String, label: String, context: Context) {
            val tfLiteModel: ByteBuffer = FileUtil.loadMappedFile(context, model)
            addThread(4)
            tflite = Interpreter(tfLiteModel, options)
            println("Success reading model: $model")
            associatedAxisLabels = FileUtil.loadLabels(context, label)
            println("Success reading label: $label")

            val inputTensor = tflite!!.getInputTensor(0)
            val inputShape = inputTensor.shape()
            val inputQuantizationParams = inputTensor.quantizationParams()
            inputSize = Size(inputShape[1], inputShape[2])
            inputINT8Params = MetadataExtractor.QuantizationParams(
                inputQuantizationParams.scale,
                inputQuantizationParams.zeroPoint
            )
            println("tfliteInputSize: (${inputShape[1]}, ${inputShape[1]})")

            if (inputTensor.dataType() == DataType.UINT8) USE_INT8 = true

            val outputTensor = tflite!!.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            val outputQuantizationParams = outputTensor.quantizationParams()
            outputSize = intArrayOf(outputShape[0], outputShape[1], outputShape[2])
            outputINT8Params = MetadataExtractor.QuantizationParams(
                outputQuantizationParams.scale,
                outputQuantizationParams.zeroPoint
            )
        }

        suspend fun specificDetection(bitmap: Bitmap?, target: String, threshold: Double = 0.25): Rect? {
            return specificDetection(bitmap, arrayOf(target), threshold)
        }

        suspend fun specificDetection(bitmap: Bitmap?, target: Array<String>, threshold: Double = 0.25): Rect? {
            val originalBitmap = bitmap ?: throw IllegalStateException("输入图像未指定")
            var rect: Rect? = null
            run sift@{
                detect(originalBitmap).forEach {
                    if (target.contains(it.labelName) && it.labelScore >= threshold && it.confidence >= threshold) {
                        rect = it.location.toRect()
                        return@sift
                    }
                }
            }
            delay(1)
            return rect
        }

        private suspend fun detect(bitmap: Bitmap?): ArrayList<Recognition> {
            val originalBitmap = bitmap ?: throw IllegalStateException("输入图像未指定")
            val tflite = tflite ?: throw IllegalStateException("模型还未载入")
            val inputSize = inputSize ?: throw IllegalStateException("输入大小未指定")
            val outputSize = outputSize ?: throw IllegalStateException("输出大小未指定")
            val associatedAxisLabels =
                associatedAxisLabels ?: throw IllegalStateException("类别标签未指定")

            var tfLiteInput =
                if (USE_INT8) TensorImage(DataType.UINT8) else TensorImage(DataType.FLOAT32)
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputSize.height, inputSize.width, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f))
                .apply {
                    if (USE_INT8) {
                        add(QuantizeOp(inputINT8Params!!.zeroPoint.toFloat(), inputINT8Params!!.scale))
                        add(CastOp(DataType.UINT8))
                    }
                }
                .build()

            val letterbox: Bitmap = letterbox(originalBitmap, inputSize)
            tfLiteInput.load(letterbox)
            tfLiteInput = imageProcessor.process(tfLiteInput)

            var probabilityBuffer: TensorBuffer =
                if (USE_INT8) TensorBuffer.createFixedSize(
                    outputSize,
                    DataType.UINT8
                ) else TensorBuffer.createFixedSize(outputSize, DataType.FLOAT32)

            tflite.run(tfLiteInput.buffer, probabilityBuffer.buffer)

            if (USE_INT8) {
                val tensorProcessor: TensorProcessor = TensorProcessor.Builder()
                    .add(DequantizeOp(outputINT8Params!!.zeroPoint.toFloat(), outputINT8Params!!.scale))
                    .build()
                probabilityBuffer = tensorProcessor.process(probabilityBuffer)
            }

            val recognitionArray = probabilityBuffer.floatArray
            val allRecognitions = ArrayList<Recognition>()

            for (i in 0 until outputSize[1]) {
                val gridStride = i * outputSize[2]
                val x = recognitionArray[gridStride] * inputSize.width
                val y = recognitionArray[gridStride + 1] * inputSize.height
                val w = recognitionArray[gridStride + 2] * inputSize.width
                val h = recognitionArray[gridStride + 3] * inputSize.height
                val xMin: Float = max(0.0, x - w / 2.0).toFloat()
                val yMin: Float = max(0.0, y - h / 2.0).toFloat()
                val xMax: Float = min(inputSize.width.toDouble(), x + w / 2.0).toFloat()
                val yMax: Float = min(inputSize.height.toDouble(), y + h / 2.0).toFloat()
                val confidence = recognitionArray[4 + gridStride]
                val classScores =
                    Arrays.copyOfRange(recognitionArray, 5 + gridStride, outputSize[2] + gridStride)

                var labelId = 0
                var maxLabelScores = 0f
                for (index in classScores.indices) {
                    if (classScores[index] > maxLabelScores) {
                        maxLabelScores = classScores[index]
                        labelId = index
                    }
                }

                val recognition =
                    Recognition(labelId, "", maxLabelScores, confidence, RectF(xMin, yMin, xMax, yMax))
                allRecognitions.add(recognition)
            }

            // 非极大抑制输出
            val nmsRecognitions: ArrayList<Recognition> = nms(allRecognitions, outputSize)
            // 第二次非极大抑制, 过滤那些同个目标识别到2个以上目标边框为不同类别的
            val nmsFilterBoxDuplicationRecognitions: ArrayList<Recognition> =
                nmsAllClass(nmsRecognitions)

            // 更新label信息
            for (recognition in nmsFilterBoxDuplicationRecognitions) {
                val labelId = recognition.labelId
                val labelName: String = associatedAxisLabels[labelId]
                recognition.labelName = labelName
            }

            // 转换成原位图坐标
            nmsFilterBoxDuplicationRecognitions.forEach {
                it.location = mapRectToBitmap(it.location.toRect(), originalBitmap, inputSize).toRectF()
            }
            delay(1)
            return nmsFilterBoxDuplicationRecognitions
        }

        /* Add a GPU agent */
        private fun addGPUDelegate() {
            val compatibilityList = CompatibilityList()
            if (compatibilityList.isDelegateSupportedOnThisDevice) {
                val delegateOptions = compatibilityList.bestOptionsForThisDevice
                val gpuDelegate = GpuDelegate(delegateOptions)
                options.addDelegate(gpuDelegate)
                println("The GPU agent was successfully added")
            } else {
                options.numThreads = 4
                println("4 threads have been added for inference")
            }
        }

        /**
         * 添加线程数
         * @param thread
         */
        private fun addThread(thread: Int) {
            println("4 threads have been added for inference")
            options.numThreads = thread
        }
    }
}