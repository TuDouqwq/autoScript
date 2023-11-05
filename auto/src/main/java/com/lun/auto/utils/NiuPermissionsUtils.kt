package com.lun.auto.utils

import android.app.Activity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lun.auto.accessibility.restartApp
import com.lun.auto.utils.NiuToastUtils.Companion.toast

class NiuPermissionsUtils(private val activity: Activity) {

    private var onPermissionChangeListener: OnPermissionChangeListener? = null

    // 权限监听接口
    interface OnPermissionChangeListener {
        fun onPermissionChanged()
    }

    fun setOnPermissionChangeListener(listener: OnPermissionChangeListener) {
        onPermissionChangeListener = listener
        authority()
    }

    private fun authority() {
        if (XXPermissions.isGranted(activity, Permission.MANAGE_EXTERNAL_STORAGE, Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Permission.NOTIFICATION_SERVICE, Permission.SYSTEM_ALERT_WINDOW)) {
            onPermissionChangeListener?.onPermissionChanged()
        } else {
            NiuDialogUtils.m2Alert("请于下个页面授予权限", activity) {
                /* 申请读写文件权限 */
                XXPermissions.with(activity)
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                    .permission(Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .permission(Permission.NOTIFICATION_SERVICE)
                    .permission(Permission.SYSTEM_ALERT_WINDOW)
                    .request(object : OnPermissionCallback {
                        override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                            if (!allGranted) {
                                toast("获取部分权限成功，但部分权限未正常授予")
                            }
                            // 成功获取所有权限
                            restartApp()
                        }

                        override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                            if (doNotAskAgain) {
                                toast("被永久拒绝授权，请手动授予权限")
                                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.startPermissionActivity(activity, permissions)
                            } else {
                                toast("获取权限失败")
                            }
                            restartApp()
                        }
                    })
            }
        }
    }
}