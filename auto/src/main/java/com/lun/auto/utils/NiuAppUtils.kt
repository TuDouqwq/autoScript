package com.lun.auto.utils

import android.content.Intent
import android.content.pm.PackageManager
import com.lun.auto.NiuApp

class NiuAppUtils {
    companion object {
        @JvmStatic
        fun launch(packageName: String): Boolean {
            return try {
                NiuApp.INS.startActivity(
                    NiuApp.INS.packageManager.getLaunchIntentForPackage(packageName)
                        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        @JvmStatic
        fun getPackageName(appName: String): String? {
            val installedApplications =
                NiuApp.INS.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            return installedApplications.firstOrNull {
                NiuApp.INS.packageManager.getApplicationLabel(it).toString() == appName
            }?.packageName
        }

        @JvmStatic
        fun launchApp(name: String): Boolean {
            val packageName = getPackageName(name)
            return packageName?.let {
                launch(it)
            } ?: false
        }
    }
}