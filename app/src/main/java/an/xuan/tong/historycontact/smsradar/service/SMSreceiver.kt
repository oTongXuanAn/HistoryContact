package an.xuan.tong.historycontact.smsradar.service

import an.xuan.tong.historycontact.api.ApiService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import an.xuan.tong.historycontact.realm.ApiCaching
import an.xuan.tong.historycontact.realm.HistoryContactConfiguration
import an.xuan.tong.historycontact.smsradar.SmsRadarService
import io.realm.Realm
import retrofit2.http.GET
import android.net.ConnectivityManager


class SMSreceiver : BroadcastReceiver() {
    private val TAG = this.javaClass.simpleName
    private val mKeyAPI: String by lazy {
        // Get Value of annotation API for save cache as KEY_CACHE
        val method = ApiService::getInfomation
        val get = method.annotations.find { it is GET } as? GET
        get?.value + ""
    }

    override fun onReceive(context: Context, intent: Intent) {

        if (isOnline(context)) {

            Log.e("antx", "Online Connect Intenet ");
        } else {

            Log.e("antx", "Conectivity Failure !!! ");
        }

        // start device
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            Log.e("antx", "onReceive sms BOOT_COMPLETED")
            val intentSms = Intent(context, SmsRadarService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentSms)
            } else {
                context.startService(intentSms)
            }
        }
        if ("android.intent.action.QUICKBOOT_POWEROFF" == intent.action) {
            Log.e("antx", "onReceive sms BOOT_COMPLETED")
        }
        if ("android.intent.action.ACTION_SHUTDOWN" == intent.action) {
            Log.e("antx", "onReceive sms ACTION_SHUTDOWN")
            //ACTION_SHUTDOWN
        }
    }

    private fun getCacheInformation(): ApiCaching? {
        val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
        val mangaSearchObj: ApiCaching? = mRealm.where(ApiCaching::class.java).contains("apiName", mKeyAPI).findFirst()
        // clone data if don't have this line -> crash app after "mRealm.close()"
        val result = ApiCaching(mangaSearchObj?.apiName, mangaSearchObj?.data, mangaSearchObj?.updateAt)
        mRealm.close()
        return result
    }

    private fun isOnline(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            return netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return false
        }

    }
}
