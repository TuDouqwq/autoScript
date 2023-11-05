package com.lun.auto.floating

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import java.util.concurrent.ConcurrentHashMap

object NiuFloat {
    private val windowMap = ConcurrentHashMap<String, NiuFloatingWindowHelper>()

    fun with(activity: Context) = FloatingWindowBuilder(activity)

    fun hide(tag: String) = windowMap[tag]?.visible(false)

    fun show(tag: String) = windowMap[tag]?.visible(true)

    fun isShow(tag: String) = windowMap[tag]?.getConfig()?.isShow ?: false

    fun getFloatView(tag: String): View? = windowMap[tag]?.getConfig()?.layoutView

    fun dismiss(tag: String) = windowMap[tag]?.remove()

    fun updateFloat(tag: String, x: Int, y: Int) = windowMap[tag]?.updateFloat(x, y)

    fun remove(floatTag: String?) = windowMap.remove(floatTag)

    class FloatingWindowBuilder(private val activity: Context) {
        private val config = NiuFloatConfig()

        fun setTag(floatTag: String) = apply {
            config.floatTag = floatTag
        }

        fun setLocation(x: Int, y: Int) = apply {
            config.locationPair = Pair(x, y)
        }

        fun setLayout(layoutId: Int, invokeView: NiuOnInvokeView? = null) = apply {
            config.layoutId = layoutId
            config.invokeView = invokeView
        }

        fun setLayout(layoutView: View, invokeView: NiuOnInvokeView? = null) = apply {
            config.layoutView = layoutView
            config.invokeView = invokeView
        }

        fun setGravity(gravity: NiuGravity) = apply {
            config.gravity = gravity
        }

        fun setFlags(flags: NiuFlags) = apply {
            config.flags = flags
        }

        fun applyConfig(block: NiuFloatConfig.() -> Unit) = apply {
            block(config)
        }

        fun setClickThrough(clickThrough: Boolean) = apply {
            config.clickThrough = clickThrough
        }

        fun setMatchParent(widthMatch: Boolean = false, heightMatch: Boolean = false) = apply {
            config.widthMatch = widthMatch
            config.heightMatch = heightMatch
        }

        fun setDragEnable(dragEnable: Boolean) = apply {
            config.dragEnable = dragEnable
        }

        fun setDragLayoutId(id: Int) = apply {
            config.dragLayoutIdList.add(id)
        }

        // 创建并显示
        @RequiresApi(Build.VERSION_CODES.O)
        fun show() {
            val floatTag = config.floatTag ?: throw IllegalStateException("Tag未指定")
//                if (windowMap[floatTag] != null) throw IllegalStateException("Tag已存在")
            if (!windowMap.containsKey(floatTag)) {
                // 不存在则创建并显示
                windowMap[floatTag] = NiuFloatingWindowHelper(activity, config)
                windowMap[floatTag]?.createWindow(true)
            } else {
                // 存在则显示
                windowMap[floatTag]?.visible(true)
            }
        }

        // 创建 但不显示
        @RequiresApi(Build.VERSION_CODES.O)
        fun create() {
            val floatTag = config.floatTag ?: throw IllegalStateException("Tag未指定")
//                if (windowMap[floatTag] != null) throw IllegalStateException("Tag已存在")
            if (!windowMap.containsKey(floatTag)) {
                // 不存在则创建
                windowMap[floatTag] = NiuFloatingWindowHelper(activity, config)
                windowMap[floatTag]?.createWindow(false)
            }
        }
    }
}