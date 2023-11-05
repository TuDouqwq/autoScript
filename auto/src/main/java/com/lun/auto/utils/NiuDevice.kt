package com.lun.auto.utils

import com.blankj.utilcode.util.DeviceUtils.getUniqueDeviceId
import com.lun.auto.NiuApp

class NiuDevice {
    companion object {
        val width: Int = NiuApp.INS.resources.displayMetrics.widthPixels

        val height: Int = NiuApp.INS.resources.displayMetrics.heightPixels
        
        val id: String = getUniqueDeviceId()
    }
}