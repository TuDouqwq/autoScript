package com.lun.auto.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.text.InputType
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.lun.auto.NiuApp.Companion.NiuComponentActivity
import com.lun.auto.accessibility.jumpToSettingPage
import com.lun.auto.accessibility.log
import com.lun.auto.sensor.SensorManagerHelper
import com.lun.auto.utils.NiuDialogUtils.Companion.alert
import com.lun.auto.utils.NiuDialogUtils.Companion.m2Alert

/**
 * # 无障碍服务
 *
 * Created on 2023/2/16
 * @author NiuMa
 */
class NiuAccessibilityService : AccessibilityService() {

    // 定义KeyEvent回调接口
    interface OnKeyEventCallback {
        fun onKeyEvent(event: KeyEvent?)
    }

    companion object {
        //无障碍高级服务 执行手势等操作
        /**
         * GestureService base on AutoScriptAccessibility
         */
        @SuppressLint("StaticFieldLeak")
        var accessibilityService: AccessibilityService? = null

        val gestureService: AccessibilityService
            get() = accessibilityService ?: throw IllegalStateException("无障碍服务未开启")

        var NiuLastActivityInfo: ActivityInfo? = null

        var NiuLastPackage: String? = null

        var NiuLastClassName: String? = null

        // KeyEvent监听器列表
        private val onKeyEventCallbacks: MutableList<OnKeyEventCallback> = mutableListOf()

        // 设置KeyEvent监听器
        fun addOnKeyEventCallback(onKeyEvent: OnKeyEventCallback) {
            onKeyEventCallbacks.add(onKeyEvent)
        }

        // 移除KeyEvent监听器
        fun removeOnKeyEventCallback(onKeyEvent: OnKeyEventCallback) {
            onKeyEventCallbacks.remove(onKeyEvent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        //must
        accessibilityService = this
        log("已开启无障碍服务")
    }

    override fun onDestroy() {
        super.onDestroy()
        //must
        accessibilityService = null
        log("已断开无障碍服务")
    }

    override fun onInterrupt() {}

    // 将 onKeyEvent 中的处理逻辑抽取出来，并调用回调实例的方法
    override fun onKeyEvent(event: KeyEvent?): Boolean {
//        println(event)
        // 调用回调接口的onKeyEvent方法
        for (callback in onKeyEventCallbacks) {
            callback.onKeyEvent(event)
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        println("pkg:${event?.packageName} text:${event?.text} event:${ event?.eventType }")

//        println(event)

        event?.let {
            if (it.packageName != null && it.className != null) {
                val componentName = ComponentName(
                    it.packageName.toString(),
                    it.className.toString()
                )
                try {
                    NiuLastActivityInfo = gestureService.packageManager?.getActivityInfo(componentName, PackageManager.GET_META_DATA)
                    NiuLastPackage = it.packageName.toString()
                    NiuLastClassName = it.className.toString()
                } catch (_: PackageManager.NameNotFoundException) {
                }
            }
        }
    }
}