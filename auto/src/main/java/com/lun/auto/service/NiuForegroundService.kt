package com.lun.auto.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.drake.net.Get
import com.lun.auto.NiuApp
import com.lun.auto.R
import kotlinx.coroutines.*
import org.json.JSONObject

/**
 * # ForegroundService
 *
 * Created on 2020/6/11
 * @author Vove
 */
class NiuForegroundService : Service() {

    companion object {
        fun startNiuForeground(application: Application) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                application.startForegroundService(Intent(application, NiuForegroundService::class.java))
            } else {
                application.startService(Intent(application, NiuForegroundService::class.java))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = Get<String>("https://v1.hitokoto.cn/").await()
                startForeground(1999, getNotification(JSONObject(data).getString("hitokoto")))
            } catch (throwable: Throwable) {
                startForeground(1999, getNotification("坚信自己 即为正道"))
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val channelId by lazy {
        val id = "ForegroundService"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(
                id,
                "前台服务",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(c)
        }
        id
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun getNotification(text: String) = NotificationCompat.Builder(this, channelId).apply {
        setContentTitle("前台服务")
        setContentText(text)
        val printIntent = Intent(this@NiuForegroundService, NiuForegroundService::class.java)
        val pi = PendingIntent.getService(
            this@NiuForegroundService,
            0,
            printIntent,
            PendingIntent.FLAG_MUTABLE
        )
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setSmallIcon(R.drawable.lun)
        val acb = NotificationCompat.Action.Builder(0, "此前台服务用于保活应用 请勿关闭", pi)
        addAction(acb.build())
        setOngoing(true)
    }.build()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}