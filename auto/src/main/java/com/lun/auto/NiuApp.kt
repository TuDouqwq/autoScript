package com.lun.auto

import android.app.Application
import androidx.activity.ComponentActivity
import com.lun.auto.service.NiuForegroundService.Companion.startNiuForeground
import com.lun.auto.utils.NiuLogUtils.initLog
import com.lun.auto.utils.NiuToastUtils.Companion.initToaster

class NiuApp {
    /**
     * Initialize before use, otherwise some functions will fail
     **/
    companion object {
        lateinit var INS: Application
        lateinit var NiuComponentActivity: ComponentActivity

        fun initApplication(application: Application) {
            INS = application
            initToaster(application)
            initLog(application)
            startNiuForeground(application)
        }

        fun initAppCompatActivity(componentActivity: ComponentActivity) {
            NiuComponentActivity = componentActivity
        }
    }
}