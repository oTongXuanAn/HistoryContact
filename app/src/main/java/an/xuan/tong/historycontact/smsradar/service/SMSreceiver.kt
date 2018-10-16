package an.xuan.tong.historycontact.smsradar.service

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.CallLogServer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import an.xuan.tong.historycontact.realm.RealmUtils
import an.xuan.tong.historycontact.smsradar.SmsRadarService
import android.net.ConnectivityManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class SMSreceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (isOnline(context)) {
            val ListCallLogFail = RealmUtils.getAllCallLog()
            for (i in ListCallLogFail) {
               /* sendCallFail(i.id, i.phone.toString(), i.datecreate.toString(), i.duration.toString(),
                        i.fileaudio.toString(), i.lat.toString(), i.lng.toString(), i.type)*/
            }

        } else {
            Log.e("antx", "Conectivity Failure !!! ")
        }

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

    //file audio local
    private fun sendCallFail(realmId: Int?, phoneNumber: String?, dateCreate: String, duration: String, fileAAudio: String, lat: String, lng: String, type: Boolean?) {
        val result: HashMap<String, String> = HashMap()
        result["Authorization"] = RealmUtils.getAuthorization()
        var id = RealmUtils.getAccountId()
        var message = CallLogServer(id, phoneNumber,
                dateCreate, duration, lat, lng, fileAAudio, type)
        id?.let {
            Repository.createService(ApiService::class.java, result).insertCallLog(message.toMap(), Constant.KEY_API)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                RealmUtils.deleteItemCachingCallLog(realmId)
                                Log.e("antx", "sendCallFail sucess")

                            }
                    ) {
                        Log.e("antx", "sendCallFail error")

                    }
        }
    }

    //file audio server
    private fun sendRecoderToServer(realmID: Int, filePath: String, number: String, dataCreate: String, duaration: String, lat: String, lng: String, typeCall: Boolean) {
        try {
            val file = File(filePath)
            val result: HashMap<String, String> = HashMap()
            result["Authorization"] = RealmUtils.getAuthorization()
            var id = RealmUtils.getAccountId()
            val temp = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            var imageFile = MultipartBody.Part.createFormData(file.name, file.name, temp)
            Repository.createService(ApiService::class.java, result).insertUpload(Constant.KEY_API, id, imageFile)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                if (result.isNotEmpty()) {
                                    sendCallFail(realmID, number, dataCreate, duaration, result[0], lat, lng, typeCall)
                                }
                            },
                            { e ->
                                Log.e("test", "sendRcoderToServer  error " + e.message)
                            })
        } catch (e: Exception) {
            Log.e("antx Exception", "sendRcoderToServer " + e.message)
        }

    }
}
