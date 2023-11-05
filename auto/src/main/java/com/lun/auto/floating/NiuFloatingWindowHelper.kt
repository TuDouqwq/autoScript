package com.lun.auto.floating

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.afollestad.materialdialogs.utils.MDUtil.inflate
import com.blankj.utilcode.util.ThreadUtils
import com.lun.auto.accessibility.log
import com.lun.auto.floating.NiuGravity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

class NiuFloatingWindowHelper(private val activity: Context, private var config: NiuFloatConfig) {
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var parentFrameLayout: FrameLayout
    private var startX: Int = 0
    private var startY: Int = 0
    private var lastX: Int = 0
    private var lastY: Int = 0

    private val longPressTime = 500L // 定义长按的时间阈值（以毫秒为单位）
    private var nowPressTime = 0L
    private var floatJob: Job? = null

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    fun createWindow(isShow: Boolean){
        parentFrameLayout = FrameLayout(activity)
        parentFrameLayout.tag = config.floatTag
        val floatingView: View = if (config.layoutView != null) {
            parentFrameLayout.addView(config.layoutView)
            parentFrameLayout
        } else {
            val layoutId = config.layoutId ?: throw IllegalStateException("布局未指定")
            LayoutInflater.from(activity).inflate(layoutId, parentFrameLayout, true)
        }

        params = WindowManager.LayoutParams().apply {
            // 设置窗口类型为应用子窗口，和PopupWindow同类型
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

            flags = if (config.flags == NiuFlags.NOT_ALLOWED) {
                // 不允许窗口扩展到屏幕外
                FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE
            } else {
                // 没有边界限制，允许窗口扩展到屏幕外
                FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_NO_LIMITS
            }

            if (config.clickThrough) {
                // 设置点击穿透
                flags += WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            }

            width = if (config.widthMatch) MATCH_PARENT else WRAP_CONTENT
            height = if (config.widthMatch) MATCH_PARENT else WRAP_CONTENT

            format = PixelFormat.RGBA_8888

            // 设置悬浮窗的位置
            gravity = when(config.gravity) {
                START_TOP -> Gravity.START or Gravity.TOP
                END_BOTTOM -> Gravity.BOTTOM or Gravity.END
                BOTTOM_MIDDLE -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                TOP_MIDDLE -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }
            x = config.locationPair.first
            y = config.locationPair.second
        }

        parentFrameLayout.visibility = if (isShow) View.VISIBLE else View.GONE

        windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        ThreadUtils.runOnUiThread {
            windowManager.addView(floatingView, params)
        }

        config.isShow = isShow

        config.invokeView?.invoke(floatingView)
        config.layoutView = floatingView

        if (config.dragEnable) {
            // 设置整个布局的拖拽
            parentFrameLayout.setOnTouchListener { _, motionEvent ->
                move(parentFrameLayout, motionEvent)
                true
            }
        }

        // 设置可以移动控件
        if (config.dragLayoutIdList.isNotEmpty()) {
            // 输出列表中的所有元素
            for (id in config.dragLayoutIdList) {
                val dragView = floatingView.findViewById<View>(id)
                dragView.setOnTouchListener { _, motionEvent ->
                    // false: 事件不被消费，即事件仍然会传递给下一个监听器或者是默认处理
                    move(dragView, motionEvent)
                }
            }
        }
    }

    private fun move(dragView: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // 按下时记录悬浮窗相对于屏幕的位置
                startX = motionEvent.rawX.toInt()
                startY = motionEvent.rawY.toInt()
                lastX = params.x
                lastY = params.y
                nowPressTime = System.currentTimeMillis()
                floatJob?.cancel()
                floatJob = CoroutineScope(Dispatchers.Default).launch {
                    delay(longPressTime)
                    dragView.performLongClick()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // 移动时更新悬浮窗的位置
                val dx = motionEvent.rawX - startX
                val dy = motionEvent.rawY - startY
                params.x = (lastX + dx).toInt()
                params.y = (lastY + dy).toInt()
                windowManager.updateViewLayout(parentFrameLayout, params)
                nowPressTime = System.currentTimeMillis()
                floatJob?.cancel()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL-> {
                if (System.currentTimeMillis() - nowPressTime > longPressTime) {
                    return true
                }
                floatJob?.cancel()
                // 检查移动距离是否足够小
                val xDiff = abs(motionEvent.rawX - startX)
                val yDiff = abs(motionEvent.rawY - startY)
                if (xDiff < 10 && yDiff < 10) {
                    // 手指移动的距离足够小，判断为单击事件
                    dragView.performClick()
                    return true
                }
            }
        }
        return false
    }

    fun updateFloat(x: Int, y: Int) {
        params.x = x
        params.y = y
        windowManager.updateViewLayout(parentFrameLayout, params)
    }

    fun visible(isShow: Boolean) {
        ThreadUtils.runOnUiThread {
            config.isShow = isShow
            if (isShow) {
                parentFrameLayout.alpha = 0.0f // 先设置透明度为 0
                parentFrameLayout.visibility = View.VISIBLE // 再设置可见
                val anim = ObjectAnimator.ofFloat(parentFrameLayout, "alpha", 0.0f, 1.0f)
                anim.duration = 180
                anim.start()
            } else {
                val anim = ObjectAnimator.ofFloat(parentFrameLayout, "alpha", 1.0f, 0.0f)
                anim.duration = 180
                anim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        parentFrameLayout.visibility = View.GONE // 动画结束后设置为不可见
                    }
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                })
                anim.start()
            }
        }
    }

    fun getConfig() = config

    // 删除当前悬浮窗
    fun remove() {
        NiuFloat.remove(config.floatTag)
        ThreadUtils.runOnUiThread {
            windowManager.run {
                removeViewImmediate(parentFrameLayout)
                log("${config.floatTag}----->删除成功")
            }
        }
    }
}