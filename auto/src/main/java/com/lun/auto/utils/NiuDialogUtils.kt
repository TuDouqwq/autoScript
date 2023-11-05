package com.lun.auto.utils

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lun.auto.R
import java.time.format.DecimalStyle
import java.util.concurrent.CountDownLatch

class NiuDialogUtils {
    companion object {
        enum class DialogType {
            CHECK, RADIO, SELECT
        }

        fun m2Alert(message: String, context: Context, buttonText: String? = "确认", style: Int? = null, finishes: (() -> Unit)? = null) {
            runOnUiThread {
                val dialogBuilder = if (style != null) {
                    MaterialAlertDialogBuilder(context, style)
                } else {
                    MaterialAlertDialogBuilder(context)
                }

                dialogBuilder.setTitle(R.string.dialog_prompt)
                    .setMessage(message)
                    .setPositiveButton(buttonText) { _, _ ->
                        finishes?.invoke()
                    }
                    .setOnDismissListener {
                        finishes?.invoke()
                    }
                    .show()
            }
        }

        // Cannot be used on the main thread
        fun alert(message: String, context: Context, useFloat: Boolean = true) {
            val latch = CountDownLatch(1)
            runOnUiThread {
                MaterialDialog(context)
                    .apply {
                        if (useFloat) {
                            window?.setType(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                            )
                        }
                    }
                    .show {
                        title(R.string.dialog_prompt)
                        cornerRadius(8f)
                        message(text = message)
                        positiveButton(click = {
                            latch.countDown()
                        })
                        onDismiss {
                            latch.countDown()
                        }
                    }
            }
            try {
                latch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        fun alert(
            message: String,
            context: Context,
            useFloat: Boolean = true,
            callback: (() -> Unit)? = null
        ) {
            runOnUiThread {
                MaterialDialog(context)
                    .apply {
                        if (useFloat) {
                            window?.setType(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                            )
                        }
                    }
                    .show {
                        title(R.string.dialog_prompt)
                        cornerRadius(8f)
                        message(text = message)
                        positiveButton(click = {
                            callback?.invoke()
                        })
                        setOnDismissListener {
                            callback?.invoke()
                        }
                    }
            }
        }

        // Cannot be used on the main thread
        fun dialogInput(
            title: String,
            context: Context,
            useFloat: Boolean = true,
            hint: String? = null,
            maxLength: Int? = null,
            prefill: String? = null
        ): CharSequence? {
            val latch = CountDownLatch(1)
            var results: CharSequence? = null
            runOnUiThread {
                MaterialDialog(context)
                    .apply {
                        if (useFloat) {
                            window?.setType(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                            )
                        }
                    }
                    .show {
                        title(text = title)
                        val input = input(hint = hint, maxLength = maxLength, prefill = prefill) { _, text ->
                            results = text
                        }
                        positiveButton(click = {
                            latch.countDown()
                        })
                        onDismiss {
                            latch.countDown()
                        }
                    }
            }

            try {
                latch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return results
        }

        fun dialogInput(
            title: String,
            context: Context,
            useFloat: Boolean = true,
            hint: String? = null,
            maxLength: Int? = null,
            prefill: String? = null,
            results: ((text: CharSequence) -> Unit)? = null
        ) {
            runOnUiThread {
                MaterialDialog(context)
                    .apply {
                        if (useFloat) {
                            window?.setType(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                            )
                        }
                    }
                    .show {
                        title(text = title)
                        val input = input(hint = hint, maxLength = maxLength, prefill = prefill) { _, text ->
                            results?.invoke(text)
                        }
                        positiveButton()
                    }
            }
        }

        // Cannot be used on the main thread
        fun dialogList(
            type: DialogType,
            title: String,
            list: List<String>,
            context: Context,
            useFloat: Boolean = true,
            useButton: Boolean = true
        ): IntArray? {
            var results: IntArray? = null
            val latch = CountDownLatch(1)
            runOnUiThread {
                MaterialDialog(context)
                    .apply {
                        if (useFloat) {
                            window?.setType(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                            )
                        }
                    }
                    .show {
                        title(text = title)
                        cornerRadius(8f)
                        when (type) {
                            DialogType.CHECK -> {
                                val selectedIndex = listItemsMultiChoice(items = list) { _, index, _ ->
                                    results = index
                                    latch.countDown()
                                }
                            }
                            DialogType.RADIO -> {
                                val selectedIndex = listItemsSingleChoice(
                                    items = list,
                                    initialSelection = 0
                                ) { _, index, _ ->
                                    results = intArrayOf(index)
                                    latch.countDown()
                                }
                            }
                            DialogType.SELECT -> {
                                val selectedIndex = listItems(items = list) { _, index, _ ->
                                    results = intArrayOf(index)
                                    latch.countDown()
                                }
                            }
                        }

                        if (useButton) positiveButton()

                        onDismiss {
                            latch.countDown()
                        }
                    }
            }
            try {
                latch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return results
        }

        fun dialogList(
            type: DialogType,
            title: String,
            list: List<String>,
            context: Context,
            useFloat: Boolean = true,
            useButton: Boolean = true,
            onItemSelected: ((selectedIndex: IntArray) -> Unit)? = null
        ) {
            runOnUiThread {
                MaterialDialog(context)
                    .apply {
                        if (useFloat) {
                            window?.setType(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                            )
                        }
                    }
                    .show {
                        title(text = title)
                        cornerRadius(8f)

                        when (type) {
                            DialogType.CHECK -> {
                                val selectedIndex = listItemsMultiChoice(items = list) { _, index, _ ->
                                    onItemSelected?.invoke(index)
                                }
                            }
                            DialogType.RADIO -> {
                                val selectedIndex = listItemsSingleChoice(
                                    items = list,
                                    initialSelection = 0
                                ) { _, index, _ ->
                                    onItemSelected?.invoke(intArrayOf(index))
                                }
                            }
                            DialogType.SELECT -> {
                                val selectedIndex = listItems(items = list) { _, index, _ ->
                                    onItemSelected?.invoke(intArrayOf(index))
                                }
                            }
                        }

                        if (useButton) positiveButton()
                    }
            }
        }

        // Cannot be used on the main thread
        fun dialogSelect(
            title: String,
            list: List<String>,
            context: Context,
            useFloat: Boolean = true,
            useButton: Boolean = true
        ): Int? {
            val dialogList = dialogList(DialogType.SELECT, title, list, context, useFloat, useButton)
            return if (dialogList != null) dialogList[0] else null
        }

        fun dialogSelect(
            title: String,
            list: List<String>,
            context: Context,
            useFloat: Boolean = true,
            useButton: Boolean = true,
            onItemSelected: ((selectedIndex: Int) -> Unit)? = null
        ) {
            dialogList(DialogType.SELECT, title, list, context, useFloat, useButton) {
                onItemSelected?.invoke(it[0])
            }
        }

        // Cannot be used on the main thread
        fun dialogRadio(
            title: String,
            list: List<String>,
            context: Context,
            useFloat: Boolean = true,
            useButton: Boolean = true
        ): Int? {
            val dialogList = dialogList(DialogType.RADIO, title, list, context, useFloat, useButton)
            return if (dialogList != null) dialogList[0] else null
        }

        fun dialogRadio(
            title: String,
            list: List<String>,
            context: Context,
            useFloat: Boolean = true,
            useButton: Boolean = true,
            onItemSelected: ((selectedIndex: Int) -> Unit)? = null
        ) {
            dialogList(DialogType.RADIO, title, list, context, useFloat, useButton) {
                onItemSelected?.invoke(it[0])
            }
        }

        // Cannot be used on the main thread
        fun dialogCheck(
            title: String,
            list: List<String>,
            context: Context,
            useFloat: Boolean = true,
            useButton: Boolean = true
        ): IntArray? {
            return dialogList(DialogType.CHECK, title, list, context, useFloat, useButton)
        }

        fun dialogCheck(
            title: String,
            list: List<String>,
            context: Context,
            useFloat: Boolean = true,
            useButton: Boolean = true,
            onItemSelected: ((selectedIndex: IntArray) -> Unit)? = null
        ) {
            dialogList(DialogType.CHECK, title, list, context, useFloat, useButton) {
                onItemSelected?.invoke(it)
            }
        }
    }
}