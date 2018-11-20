package an.xuan.tong.historycontact.call.receiver

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.util.Log


class CallRecService : Service() {
    lateinit var processing: IProcessing

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        processing = CallRecProcessingNotification(this)
        processing.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return  processing.onStartCommand(intent, flags, startId)

    }

    override fun onDestroy() {
        Log.e("antx","CallRecService onDestroy")
        processing.onDestroy()
        super.onDestroy()
    }
}

