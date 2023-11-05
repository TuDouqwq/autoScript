package com.lun.autoscript.script

import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.graphics.Path
import android.os.Build
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.ShellUtils.execCmd
import com.lun.auto.accessibility.*
import com.lun.auto.utils.NiuShellUtils.execCommand
import com.lun.auto.utils.NiuToastUtils
import com.lun.autoscript.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader


object MainScript {
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("SdCardPath")
    fun main() = CoroutineScope(Dispatchers.Default).launch {
        NiuToastUtils.toast("开始查找")

        val path = Path().apply {
            moveTo(900.toFloat(), 775.toFloat())
            lineTo(1439.toFloat(), 788.toFloat())
        }
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 2000)
        val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription).build()
        executeGesture(gestureDescription)
    }
}