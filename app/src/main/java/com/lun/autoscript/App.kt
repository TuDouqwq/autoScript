package com.lun.autoscript

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import com.lun.auto.NiuApp

class App : Application(){
    companion object {
        lateinit var INS: Application
    }

    /**
     *  App 启动时调用
     * */
    override fun onCreate() {
        super.onCreate()
        INS = this
        NiuApp.initApplication(this)
    }

    /**
     * 在配置改变时调用 例如竖屏变横屏
     * */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}