package com.lun.auto.utils

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.lun.auto.NiuApp

class NiuDrawUtils {

    private val paint = Paint()
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var rect: Rect = Rect(0,0,0,0)
    private val handler = Handler(Looper.getMainLooper())

    init {
        // 在构造函数中创建和添加View
        handler.post {
            overlayView = object : View(NiuApp.INS) {
                override fun onDraw(canvas: Canvas?) {
                    super.onDraw(canvas)
                    canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    paint.color = Color.GREEN // 颜色
                    paint.style = Paint.Style.STROKE // 只画边框
                    paint.strokeWidth = 3f // 粗
                    canvas?.drawRect(this@NiuDrawUtils.rect, paint)
                }
            }

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )

            windowManager = NiuApp.INS.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.addView(overlayView, layoutParams)
        }
    }

    /***
     * 绘制方框
     * @param rect 方框的范围
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun drawBox(rect: Rect) {
        this.rect = rect
        handler.post {
            overlayView.invalidate()
        }
    }

    /**
     * 结束绘制
     * */
    fun endDrawing() {
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
