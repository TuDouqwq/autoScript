@file: JvmName("gesture_api")
package com.lun.auto.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.blankj.utilcode.util.ThreadUtils
import com.lun.auto.service.NiuAccessibilityService.Companion.gestureService
import java.util.concurrent.CompletableFuture

fun click(x: Int, y: Int): Boolean = press(x, y, (ViewConfiguration.getTapTimeout() + 50).toLong())

fun press(bounds: Rect, time: Long): Boolean = press(bounds.centerX(), bounds.centerY(), time)

fun press(x: Int, y: Int, time: Long): Boolean {
    val path = Path()
    path.moveTo(x.toFloat(), y.toFloat())
    val gestureDescription = GestureDescription.Builder()
        .addStroke(GestureDescription.StrokeDescription(path, 0, time))
        .build()
    return executeGesture(gestureDescription)
}

fun press(x: Double, y: Double, time: Long): Boolean {
    return press(x.toInt(), y.toInt(), time)
}

fun swipe(
    x1: Int,
    y1: Int,
    x2: Int,
    y2: Int,
    time: Long
): Boolean {
    val path = Path().apply {
        moveTo(x1.toFloat(), y1.toFloat())
        lineTo(x2.toFloat(), y2.toFloat())
    }
    val strokeDescription = GestureDescription.StrokeDescription(path, 0, time)
    val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription).build()
    return executeGesture(gestureDescription)
}

fun executeGesture(description: GestureDescription): Boolean {
    val result = CompletableFuture<Boolean>()

    val callback = object : AccessibilityService.GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription) {
            result.complete(true)
        }

        override fun onCancelled(gestureDescription: GestureDescription) {
            result.complete(false)
        }
    }

    ThreadUtils.runOnUiThread {
        gestureService.dispatchGesture(description, callback, null)
    }

    return result.get()
}

val AccessibilityNodeInfo.bounds
    get() :Rect = Rect().apply { getBoundsInScreen(this) }

val AccessibilityWindowInfo.bounds
    get() :Rect = Rect().apply { getBoundsInScreen(this) }

fun AccessibilityNodeInfo.click(time: Long = 250) {
    if (this.isClickable) {
        this.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    } else {
        press(this.bounds, time)
    }
}

fun AccessibilityNodeInfo.longClick() {
    if (this.isClickable) {
        this.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    } else {
        press(this.bounds, 1000)
    }
}

fun AccessibilityNodeInfo.press(
    time: Long,
    addX: Int = 0,
    subX: Int = 0,
    addY: Int = 0,
    subY: Int = 0
) {
    val x = this.bounds.centerX() + addX - subX
    val y = this.bounds.centerY() + addY - subY
    press(x, y, time)
}