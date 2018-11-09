package an.xuan.tong.historycontact.call.receiver

import android.content.Intent


interface IProcessing {
    fun onCreate()
    fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    fun onDestroy()
}