package com.lun.auto.utils

import android.content.Context
import com.lun.auto.R
import org.junit.Test
import java.util.concurrent.CountDownLatch

class NiuDialogUtilsTest {


    /**Should display an alert with the given message and default settings*/
    @Test
    fun alertWithDefaultSettings() {
        val context = mock(Context::class.java)
        val countDownLatch = CountDownLatch(1)

        NiuDialogUtils.alert(
            context,
            R.string.app_name,
            R.string.app_name,
            object : NiuDialogUtils.DialogCallback {
                override fun onDialogDismiss() {
                    countDownLatch.countDown()
                }
            })

        countDownLatch.await()

        verify(context, times(1)).getString(R.string.app_name)
    }

    /**Should display an alert with the given message and useFloat set to true*/
    @Test
    fun alertWithUseFloatTrue() {
        val context = mock(Context::class.java)
        val countDownLatch = CountDownLatch(1)

        NiuDialogUtils.alert(
            context,
            R.string.alert_title,
            R.string.alert_message,
            true,
            object : NiuDialogUtils.DialogCallback {
                override fun onPositiveButtonClicked() {
                    countDownLatch.countDown()
                }

                override fun onNegativeButtonClicked() {
                    // do nothing
                }
            })

        countDownLatch.await()

        verify(context, times(1)).getString(R.string.alert_title)
        verify(context, times(1)).getString(R.string.alert_message)
    }

    /**Should display an alert with the given message and useFloat set to false*/
    @Test
    fun alertWithUseFloatFalse() {
        val context = mock(Context::class.java)
        val countDownLatch = CountDownLatch(1)

        NiuDialogUtils.alert(
            context,
            R.string.dialog_title,
            R.string.dialog_message,
            false,
            object : NiuDialogUtils.DialogCallback {
                override fun onPositiveButtonClicked() {
                    countDownLatch.countDown()
                }

                override fun onNegativeButtonClicked() {
                    // do nothing
                }
            })

        countDownLatch.await()

        verify(context, times(1)).getString(R.string.dialog_title)
        verify(context, times(1)).getString(R.string.dialog_message)
    }

}