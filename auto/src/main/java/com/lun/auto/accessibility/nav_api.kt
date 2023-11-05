@file: JvmName("nav_api")
package com.lun.auto.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Build
import androidx.annotation.RequiresApi
import com.lun.auto.service.NiuAccessibilityService.Companion.gestureService

//返回操作
fun back(): Boolean = gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)

//返回桌面
fun home(): Boolean = gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)

// 收起键盘
fun modeHidden(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.SHOW_MODE_HIDDEN)

//电源菜单
fun powerDialog(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)

//展开通知栏 > 快捷设置
fun quickSettings(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)

//锁屏
@RequiresApi(Build.VERSION_CODES.P)
fun lockScreen(): Boolean = gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)

//截屏
@RequiresApi(Build.VERSION_CODES.P)
fun screenShot(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)

// 打开通知栏
fun notificationBar(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)

//最近任务
fun recent(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)

//分屏
fun splitScreen(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)

// 向下滑动手势。
fun swipeDown(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GESTURE_SWIPE_DOWN)

// 向左滑动的手势。
fun swipeLeft(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GESTURE_SWIPE_LEFT)

// 向上滑动手势。
fun swipeUp(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GESTURE_SWIPE_UP)

// 向右滑动的手势。
fun swipeRight(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GESTURE_SWIPE_RIGHT)

// 触发辅助功能快捷方式的操作。
@RequiresApi(Build.VERSION_CODES.S)
fun accessibilityShortcut(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT)

// 关闭通知栏
@RequiresApi(Build.VERSION_CODES.S)
fun notificationShade(): Boolean =
    gestureService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)