@file: JvmName("global_fun_api")
package com.lun.auto.accessibility

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.blankj.utilcode.util.AppUtils
import com.drake.net.scope.AndroidScope
import com.elvishew.xlog.XLog
import com.lun.auto.NiuApp
import com.lun.auto.console.NiuConsole.Companion.consoleLog
import com.lun.auto.service.NiuAccessibilityService
import com.lun.auto.service.NiuAccessibilityService.Companion.NiuLastClassName
import com.lun.auto.service.NiuAccessibilityService.Companion.NiuLastPackage
import kotlinx.coroutines.Job

fun log(msg: Any?, console: Boolean = true) {
    XLog.d(msg)
    if (console) consoleLog(msg.toString())
}

fun log(e: Throwable) {
    XLog.e("TypeError:", e)
}

inline fun time(block: () -> Unit) {
    val startTime = System.currentTimeMillis()
    block()
    val endTime = System.currentTimeMillis()
    println("Time taken: ${endTime - startTime}ms")
}

fun exit(): Unit = AppUtils.exitApp()

fun restartApp() {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    val packageName = NiuApp.INS.packageName
    val pm = NiuApp.INS.packageManager
    val info = pm.getLaunchIntentForPackage(packageName)
    if (info != null) {
        val componentName = info.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        NiuApp.INS.startActivity(mainIntent)
        exit()
    }
}

fun launch(packageName: String) {
    NiuApp.INS.packageManager.getLaunchIntentForPackage(packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        NiuApp.INS.startActivity(this)
    }
}

fun currentPackage() = NiuLastPackage

fun currentActivity() = NiuLastClassName

suspend fun Job.await() = this.join()

/**
 * 判断是否有辅助功能权限
 * @return true 已开启
 *         false 未开启
 */
fun isAccessibilityServiceEnabled(): Boolean {
    val expectedComponentName = ComponentName(NiuApp.INS, NiuAccessibilityService::class.java)

    val enabledServicesSetting = Settings.Secure.getString(
        NiuApp.INS.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )

    return enabledServicesSetting
        ?.split(':')
        ?.mapNotNull { ComponentName.unflattenFromString(it) }
        ?.any { it == expectedComponentName } ?: false
}

/**
 * 跳转到无障碍服务设置页面
 */
fun jumpToSettingPage() {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    NiuApp.INS.startActivity(intent)
}