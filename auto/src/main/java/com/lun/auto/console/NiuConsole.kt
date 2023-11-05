package com.lun.auto.console

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.lun.auto.NiuApp
import com.lun.auto.R
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern

class NiuConsole: FrameLayout {

    companion object {
        internal val controller = NiuConsoleController()

        fun consoleLog(o: CharSequence?) {
            controller.writeLine(o)
        }

        fun showConsole() {
            EasyFloat.with(NiuApp.INS).setTag("console").setShowPattern(ShowPattern.ALL_TIME).setLocation(1000, 0).setLayout(R.layout.console).show()
        }
    }

    private val textView: TextView

    init {
        controller.add(this)
        LayoutInflater.from(context).inflate(R.layout.console_content, this)

        textView = findViewById(R.id.console_text)
    }

    internal fun printScroll() {
        controller.printTo(textView)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}