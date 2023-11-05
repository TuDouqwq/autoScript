package com.lun.autoscript

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.util.ApkUtil
import com.blankj.utilcode.util.ThreadUtils
import com.drake.net.Get
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.lun.auto.NiuApp
import com.lun.auto.accessibility.*
import com.lun.auto.console.NiuConsole
import com.lun.auto.images.NiuImages
import com.lun.auto.service.NiuAccessibilityService
import com.lun.auto.utils.NiuDialogUtils.Companion.m2Alert
import com.lun.auto.utils.NiuFilesUtils
import com.lun.auto.utils.NiuPermissionsUtils
import com.lun.auto.utils.NiuToastUtils
import com.lun.autoscript.floating.MainFloat
import com.lun.autoscript.interceptor.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        lateinit var mainAppCompatActivity: AppCompatActivity
    }

    @SuppressLint("SdCardPath")
    private val logPath = "/sdcard/autoScript/log/log.txt"

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<SwitchMaterial>(R.id.accessibilityPermissions).setOnClickListener(this)
        findViewById<FloatingActionButton>(R.id.run).setOnClickListener(this)
        findViewById<View>(R.id.viewLog).setOnClickListener(this)

        // 设置全局异常拦截器
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler())
        mainAppCompatActivity = this
        NiuApp.initAppCompatActivity(this)
        NiuPermissionsUtils(this).setOnPermissionChangeListener(object : NiuPermissionsUtils.OnPermissionChangeListener {
            override fun onPermissionChanged() {

            }
        })
        NiuConsole.showConsole()
        MainFloat.mainFloatFun()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.accessibilityPermissions -> {
                doAccessibilityPermissions()
            }
            R.id.run -> {
                doMain()
            }
            R.id.viewLog -> {
                doViewLog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun doMain() {
        if (isAccessibilityServiceEnabled()) {
            findViewById<FloatingActionButton>(R.id.run).setImageResource(R.mipmap.icon_zanting)
            /* 申请截图权限 */
//            NiuImages.requestCapture()
        } else {
            NiuToastUtils.vwToast("请先开启无障碍", this)
        }
    }

    private fun doAccessibilityPermissions() {
        if (NiuAccessibilityService.accessibilityService == null) {
            jumpToSettingPage()
        } else {
            NiuAccessibilityService.accessibilityService!!.disableSelf()
        }
    }

    private fun doViewLog() {
        val file = File(logPath)
        val uri = FileProvider.getUriForFile(App.INS, "external_files.file-provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "text/plain")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            val chooserIntent = Intent.createChooser(intent, "请选择要打开的应用")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            App.INS.startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            NiuFilesUtils.write(logPath, "")
            NiuToastUtils.toast("日志文件暂时不存在")
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<SwitchMaterial>(R.id.accessibilityPermissions).isChecked = NiuAccessibilityService.accessibilityService != null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        NiuImages.createVirtualDisplay(requestCode, resultCode, data,
            isScreenCaptureEnable = true,
            isMediaRecorderEnable = true,
            landscape = true
        )
    }
}