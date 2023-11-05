package com.lun.autoscript.floating

import android.annotation.SuppressLint
import android.os.Build
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.lun.auto.floating.NiuFloat
import com.lun.autoscript.App
import com.lun.autoscript.R
import com.lun.autoscript.script.MainScript
import com.lzf.easyfloat.EasyFloat

import kotlinx.coroutines.Job

object MainFloat {
    @SuppressLint("NewApi")
    fun mainFloatFun() {
        NiuFloat.with(App.INS)
            .setTag("runFloat")
            .setDragLayoutId(R.id.c)
            .setDragLayoutId(R.id.logo)
            .setDragLayoutId(R.id.runScript)
            .setLayout(R.layout.card_float) {view ->
                val runScriptImageView: ImageView = view.findViewById(R.id.runScript)

                runScriptImageView.setOnClickListener {
                    MainScript.main()
                }
            }.show()
    }
}
