@file: JvmName("view_finder_api")
package com.lun.auto.accessibility

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.elvishew.xlog.XLog
import com.lun.auto.service.NiuAccessibilityService.Companion.gestureService
import com.lun.auto.accessibility.UiSelectorType.*
import kotlinx.coroutines.delay


fun find(
    uiSelector: UiSelector? = null,
    max: Int? = null,
    active: Boolean = false
): MutableList<AccessibilityNodeInfo>? {
    val allRootWindowList = mutableListOf<AccessibilityNodeInfo>()

    try {
        if (!active) {
            run traversal@{
                gestureService.windows?.forEach { window ->
                    if ((max != null) && (allRootWindowList.size >= max)) return@traversal
                    window.root?.run {
                        recursiveFind(this, max, uiSelector, allRootWindowList)
                    }
                }
            }
        } else {
            gestureService.rootInActiveWindow?.run {
                recursiveFind(this, max, uiSelector, allRootWindowList)
            }
        }
    } catch (throwable: Throwable) {
        log(throwable)
    }

    return allRootWindowList.takeIf { it.isNotEmpty() }
}

private fun recursiveFind(
    rootInfo: AccessibilityNodeInfo,
    max: Int? = null,
    uiSelector: UiSelector? = null,
    allRootWindowList: MutableList<AccessibilityNodeInfo>
) {
    if (rootInfo.childCount != 0) {
        for (i in 0 until rootInfo.childCount) {
            if ((max != null) && (allRootWindowList.size >= max)) break
            rootInfo.getChild(i)?.run {
                if (childCount != 0) {
                    isType(this, uiSelector, allRootWindowList)
                    recursiveFind(this, max, uiSelector, allRootWindowList)
                } else {
                    isType(this, uiSelector, allRootWindowList)
                }
            }
        }
    }
}

private fun isType(
    rootInfo: AccessibilityNodeInfo,
    uiSelector: UiSelector? = null,
    allRootWindowList: MutableList<AccessibilityNodeInfo>
) {
    uiSelector?.run {
        when (type) {
            TEXT -> if (value == rootInfo.text) allRootWindowList.add(rootInfo)
            TEXT_LIKE -> {
                val regex = Regex(pattern = value)
                val matched = regex.containsMatchIn(input = rootInfo.str.toString())
                if (matched) allRootWindowList.add(rootInfo)
            }
            CLASS_NAME -> if (value == rootInfo.className) allRootWindowList.add(rootInfo)
            ID -> if (value == rootInfo.viewIdResourceName?.split(":id/")
                    ?.get(1)
            ) allRootWindowList.add(rootInfo)
            DESC -> if (value == rootInfo.contentDescription) allRootWindowList.add(rootInfo)
        }
    } ?: allRootWindowList.add(rootInfo)
}

suspend fun UiSelector.findOne(
    time: Long? = null,
    active: Boolean = false
): AccessibilityNodeInfo? {
    val startTime = System.currentTimeMillis()
    while (true) {
        val findResults: MutableList<AccessibilityNodeInfo>? =
            find(uiSelector = this, max = 1, active = active)
        if (findResults != null && findResults.size != 0) return findResults[0]
        if ((time != null) && ((System.currentTimeMillis() - startTime) > time)) return null
        delay(1)
    }
}

suspend fun UiSelector.findOnce(
    index: Int,
    always: Boolean = true,
    active: Boolean = false
): AccessibilityNodeInfo? {
    while (always) {
        val findResults: MutableList<AccessibilityNodeInfo>? =
            find(uiSelector = this, max = index + 1, active = active)
        if (findResults?.size == (index + 1)) return findResults[index]
        delay(1)
    }
    val findResults: MutableList<AccessibilityNodeInfo>? =
        find(uiSelector = this, max = index + 1, active = active)
    return if (findResults?.size == (index + 1)) {
        findResults[index]
    } else {
        null
    }
}

fun setText(
    index: Int,
    text: CharSequence,
    active: Boolean = false,
    appends: Boolean = false
): Boolean {
    val node =
        find(uiSelector = className("android.widget.EditText"), max = (index + 1), active = active)
            ?.getOrNull(index)
    node?.let {
        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                if (appends) "${it.text}$text" else text
            )
        }
        return it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }
    return false
}

fun input(index: Int, text: String, active: Boolean = false): Boolean {
    return setText(index = index, text = text, active = active, appends = true)
}