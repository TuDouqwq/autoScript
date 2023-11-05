@file: JvmName("ui_global_selector_api")
package com.lun.auto.accessibility

import android.view.accessibility.AccessibilityNodeInfo

enum class UiSelectorType {
    TEXT,
    TEXT_LIKE,
    CLASS_NAME,
    ID,
    DESC
}

data class UiSelector(val type: UiSelectorType, val value: String)

fun text(text: String): UiSelector {
    return UiSelector(UiSelectorType.TEXT, text)
}

fun textLike(text: String): UiSelector {
    return UiSelector(UiSelectorType.TEXT_LIKE, text)
}

fun className(className: String): UiSelector {
    return UiSelector(UiSelectorType.CLASS_NAME, className)
}

fun id(id: String): UiSelector {
    return UiSelector(UiSelectorType.ID, id)
}

fun desc(desc: String): UiSelector {
    return UiSelector(UiSelectorType.DESC, desc)
}

val AccessibilityNodeInfo.str: CharSequence?
    get() = this.text