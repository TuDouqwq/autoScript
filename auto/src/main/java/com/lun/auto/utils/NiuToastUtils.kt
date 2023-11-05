package com.lun.auto.utils

import android.app.Activity
import android.app.Application
import androidx.core.content.res.ResourcesCompat
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.CustomToastStyle
import com.lun.auto.R
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class NiuToastUtils {
    companion object {
        fun initToaster(application: Application) {
            Toaster.init(application)
        }

        fun toast(text: String) = Toaster.show(text)

        fun iToast(text: String) {
            val params = ToastParams()
            params.text = text
            params.style = CustomToastStyle(R.drawable.toast_info)
            Toaster.show(params)
        }

        fun eToast(text: String) {
            val params = ToastParams()
            params.text = text
            params.style = CustomToastStyle(R.drawable.toast_error)
            Toaster.show(params)
        }

        fun sToast(text: String) {
            val params = ToastParams()
            params.text = text
            params.style = CustomToastStyle(R.drawable.toast_success)
            Toaster.show(params)
        }

        fun wToast(text: String) {
            val params = ToastParams()
            params.text = text
            params.style = CustomToastStyle(R.drawable.toast_warn)
            Toaster.show(params)
        }

        fun vsToast(text: String, context: Activity, time: Int = 2000) = MotionToast.darkToast(
            context = context,
            message = text,
            style = MotionToastStyle.SUCCESS,
            position = MotionToast.GRAVITY_BOTTOM,
            duration = time.toLong(),
            font = ResourcesCompat.getFont(
                context, R.font.helvetica_regular
            )
        )

        fun veToast(text: String, context: Activity, time: Int = 2000) = MotionToast.darkToast(
            context = context,
            message = text,
            style = MotionToastStyle.ERROR,
            position = MotionToast.GRAVITY_BOTTOM,
            duration = time.toLong(),
            font = ResourcesCompat.getFont(
                context, R.font.helvetica_regular
            )
        )

        fun viToast(text: String, context: Activity, time: Int = 2000) = MotionToast.darkToast(
            context = context,
            message = text,
            style = MotionToastStyle.INFO,
            position = MotionToast.GRAVITY_BOTTOM,
            duration = time.toLong(),
            font = ResourcesCompat.getFont(
                context, R.font.helvetica_regular
            )
        )

        fun vwToast(text: String, context: Activity, time: Int = 2000) = MotionToast.darkToast(
            context = context,
            message = text,
            style = MotionToastStyle.WARNING,
            position = MotionToast.GRAVITY_BOTTOM,
            duration = time.toLong(),
            font = ResourcesCompat.getFont(
                context, R.font.helvetica_regular
            )
        )
    }
}