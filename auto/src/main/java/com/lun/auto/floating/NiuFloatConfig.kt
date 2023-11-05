package com.lun.auto.floating

import android.view.View

data class NiuFloatConfig(
    // 浮窗的xml布局文件
    var layoutId: Int? = null,
    var layoutView: View? = null,
    // 当前浮窗的tag
    var floatTag: String? = null,
    // 固定的初始坐标，左上角坐标
    var locationPair: Pair<Int, Int> = Pair(0, 0),
    // 是否显示
    var isShow: Boolean = false,

    var gravity: NiuGravity = NiuGravity.START_TOP,
    var flags: NiuFlags = NiuFlags.NOT_ALLOWED,
    // 是否正在被拖拽
    var isDrag: Boolean = false,
    // Callbacks
    var invokeView: NiuOnInvokeView? = null,
    // 用于设置拖拽的布局Id
    val dragLayoutIdList: MutableList<Int> = mutableListOf(),
    // 是否开启整个布局的拖拽
    var dragEnable: Boolean = false,
    // 点击穿透
    var clickThrough: Boolean = false,
    // 宽度是否充满屏幕
    var widthMatch: Boolean = false,
    // 高度是否充满屏幕
    var heightMatch: Boolean = false
)

enum class NiuGravity {
    // 左上角
    START_TOP,
    // 右下角
    END_BOTTOM,
    // 底中间
    BOTTOM_MIDDLE,
    // 顶中间
    TOP_MIDDLE,
}

enum class NiuFlags {
    // 不允许扩展屏幕外
    NOT_ALLOWED,
    // 允许扩展屏幕外
    AllOW
}