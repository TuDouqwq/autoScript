package com.lun.auto.console

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import java.lang.ref.WeakReference
import java.util.ArrayList

internal class NiuConsoleController {
    private val consoles: MutableList<WeakReference<NiuConsole>> = ArrayList()

    private val printBufferHandler: Handler by lazy { PrintBufferHandler(this) }

    private val isUIThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    private val lock = Any()

    private var text: CharSequence? = null

    fun add(console: NiuConsole) {
        consoles.add(WeakReference(console))
    }

    fun writeLine(o: CharSequence?) {
        text = o
        runBufferPrint()
    }

    fun printTo(text: TextView) {
        print(text)
    }

    private fun print(textView: TextView) {
        synchronized(lock) {
            textView.text = text
            textView.layoutParams = textView.layoutParams
        }
    }

    private fun runBufferPrint() {
        if (!isUIThread) {
            if (!printBufferHandler.hasMessages(PRINT_BUFFER)) {
                printBufferHandler.obtainMessage(PRINT_BUFFER).sendToTarget()
            }
            return
        }

        val iterator = consoles.iterator()
        while (iterator.hasNext()) {
            val console = iterator.next().get()
            if (console == null) {
                iterator.remove()
            } else {
                console.printScroll()
            }
        }
    }

    private class PrintBufferHandler(val controller: NiuConsoleController) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == PRINT_BUFFER) {
                controller.runBufferPrint()
            }
        }
    }

    companion object {
        const val PRINT_BUFFER = 653276
    }
}