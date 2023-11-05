package com.lun.auto.console

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.lun.auto.NiuApp
import com.lun.auto.R
import com.lun.auto.floating.NiuFloat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 调试窗口主题
 */
enum class NiuDevTheme {
    DARK,
    LIGHT
}

class NiuDevConsole {
    companion object {
        private val activity = NiuApp.NiuComponentActivity

        private const val floatTag = "devConsole"

        private const val devButtonText = "F1"

        private var consoleHeight = 110
        private var consoleWidth = 210
        private var consoleTextSize = 12
        private const val minWidth = 132 /** 最小窗口宽度 */
        private const val maxSize = 500 /** 输出文本的最大大小 */

        private var theme = NiuDevTheme.DARK
        private var dX: Float = 0.toFloat()
        private var dY: Float = 0.toFloat()

        private val currentTime: String
            get() {
                val df = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                return df.format(Calendar.getInstance().time)
            }

        @SuppressLint("StaticFieldLeak")
        private lateinit var tvConsole: TextView
        @SuppressLint("StaticFieldLeak")
        private lateinit var consoleScrollView: ScrollView

        @RequiresApi(Build.VERSION_CODES.O)
        fun devConsole(devFun: (() -> Unit)? = null) {
            NiuFloat.with(activity)
                .setDragLayoutId(R.id.iv_tools_minify)
                .setDragLayoutId(R.id.iv_tools_close_button)
                .setDragLayoutId(R.id.bottom_layer)
                .setDragLayoutId(R.id.top_floor)
                .setTag(floatTag)
                .setLocation(50, 200)
                .setLayout(R.layout.niu_debugkit_fragment_dev_tools) { view ->
                    val llButtonContainer: LinearLayout = view.findViewById(R.id.ll_button_container)
                    val button = LayoutInflater.from(view.context).inflate(R.layout.niu_debugkit_function_button_dark, llButtonContainer, false) as Button

                    button.text = devButtonText
                    llButtonContainer.addView(button)

                    consoleScrollView = view.findViewById(R.id.sv_console_scroll_view)
                    consoleScrollView.layoutParams.height = dpTopX(consoleHeight)
                    consoleScrollView.layoutParams.width = dpTopX(consoleWidth)

                    tvConsole = view.findViewById(R.id.tv_console)
                    tvConsole.layoutParams.height = dpTopX(consoleHeight)
                    tvConsole.layoutParams.width = dpTopX(consoleWidth)
                    tvConsole.minimumHeight = dpTopX(consoleHeight)
                    tvConsole.setTextSize(TypedValue.COMPLEX_UNIT_DIP, consoleTextSize.toFloat())

                    // 缩放
                    val ivToolsMinify: ImageView = view.findViewById(R.id.iv_tools_minify)
                    ivToolsMinify.apply {
                        setTag(id, false)
                        setOnClickListener {
                            switchMinify(this)
                        }
                    }

                    // 关闭
                    val ivToolsClose: ImageView = view.findViewById(R.id.iv_tools_close_button)
                    ivToolsClose.setOnClickListener {
                        NiuFloat.hide(floatTag)
                    }

                    // 调式按钮
                    button.setOnClickListener {
                        devFun?.invoke()
                    }

                    applyTheme()
                    softLog("ready")
            }.show()
        }

        /**
         * 查找触摸事件所在的 ScrollView
         */
        private fun findScrollView(parent: View, x: Int, y: Int): ScrollView? {
            if (parent !is ViewGroup) {
                return null
            }
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child is ScrollView && isPointInView(child, x, y)) {
                    return child
                } else {
                    val scrollView = findScrollView(child, x, y)
                    if (scrollView != null) {
                        return scrollView
                    }
                }
            }
            return null
        }

        /**
         * 判断点 (x, y) 是否在视图 view 中
         */
        private fun isPointInView(view: View, x: Int, y: Int): Boolean {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + view.width
            val bottom = top + view.height
            return x in left..right && y in top..bottom
        }

        private fun softLog(message: String) {
            val sb = StringBuilder(tvConsole.text)
            sb.append(currentTime).append("   ")
            sb.append(message)
            write(sb.toString())
        }

        fun log(string: Any?) {
            val sb = StringBuilder(tvConsole.text)
            sb.append("\n")
            sb.append(currentTime).append("   ")
            sb.append(string)
            if (sb.length > 500) {
                sb.delete(0, sb.length - 500)
            }
            write(sb.toString())
        }

        /**
         * Switch the tool to minify mode.
         */
        private fun switchMinify(toolsMinify: ImageView) {

            val rotateAnimation: RotateAnimation
            val heightValueAnimator: ValueAnimator
            val widthValueAnimator: ValueAnimator

            if (toolsMinify.getTag(toolsMinify.id) as Boolean) {
                rotateAnimation = RotateAnimation(
                    180f,
                    0f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                heightValueAnimator = ValueAnimator.ofInt(0, dpTopX(consoleHeight))
                widthValueAnimator = ValueAnimator.ofInt(dpTopX(minWidth), dpTopX(consoleWidth))
                toolsMinify.setTag(toolsMinify.id, false)
            } else {
                rotateAnimation = RotateAnimation(
                    0f,
                    180f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                heightValueAnimator = ValueAnimator.ofInt(dpTopX(consoleHeight), 0)
                widthValueAnimator = ValueAnimator.ofInt(dpTopX(consoleWidth), dpTopX(minWidth))
                toolsMinify.setTag(toolsMinify.id, true)
            }

            heightValueAnimator.duration = 200
            heightValueAnimator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                consoleScrollView.layoutParams.height = value
                consoleScrollView.requestLayout()
            }
            widthValueAnimator.duration = 200
            widthValueAnimator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                tvConsole.layoutParams.width = value
                tvConsole.requestLayout()
            }

            rotateAnimation.duration = 200
            rotateAnimation.fillAfter = true
            toolsMinify.startAnimation(rotateAnimation)
            heightValueAnimator.interpolator = AccelerateInterpolator()
            heightValueAnimator.start()
            widthValueAnimator.interpolator = AccelerateInterpolator()
            widthValueAnimator.start()
        }

        /**
         * This method will be called on build. You can call this method if you want to change the
         * theme of the console theme at runtime.
         */
        private fun applyTheme() {
            when (theme) {
                NiuDevTheme.LIGHT -> {
                    tvConsole.setBackgroundColor(getColor(R.color.debug_kit_primary_light))
                    tvConsole.setTextColor(getColor(R.color.debug_kit_background_black_light))
                }
                else -> {
                    tvConsole.setBackgroundColor(getColor(R.color.debug_kit_background_black))
                    tvConsole.setTextColor(getColor(R.color.debug_kit_primary))
                }
            }

            tvConsole.setTextSize(TypedValue.COMPLEX_UNIT_DIP, consoleTextSize.toFloat())
        }

        private fun getColor(resId: Int): Int {
            return activity.resources.getColor(resId)
        }

        private fun write(string: String) {
            tvConsole.text = string

            tvConsole.post {
                tvConsole.requestLayout()
                consoleScrollView.post {
                    consoleScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    consoleScrollView.requestLayout()
                }
            }
        }

        private fun dpTopX(dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                activity.resources.displayMetrics
            ).roundToInt()
        }
    }
}