package an.xuan.tong.historycontact.call.receiver

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.Utils.CurrentTime
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.CallLogServer
import an.xuan.tong.historycontact.call.helper.PrefsHelper
import an.xuan.tong.historycontact.location.LocationCurrent
import an.xuan.tong.historycontact.realm.CachingCallLog
import an.xuan.tong.historycontact.realm.HistoryContactConfiguration
import an.xuan.tong.historycontact.realm.RealmUtils
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Handler
import android.util.Log
import com.facebook.accountkit.internal.AccountKitController
import com.facebook.accountkit.internal.AccountKitController.getApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.io.IOException
import java.util.Date
import kotlin.collections.HashMap


/**
 * Created by aykutasil on 19.10.2016.
 */
class CallRecordReceiver : PhoneCallReceiver {

    protected lateinit var callRecord: CallRecord
    private var audiofile: File? = null
    private var isRecordStarted = false
    var startTime = 0L
    lateinit var mContext: Context

    constructor()
    constructor(callRecord: CallRecord) {
        this.callRecord = callRecord
    }

    override fun onIncomingCallReceived(context: Context, number: String, start: Date) {
        Log.d("antx", "call onIncomingCallReceived")
    }

    override fun onIncomingCallAnswered(context: Context, number: String, start: Date) {
        Log.d("antx", "call onIncomingCallAnswered")
        //startRecord(context, "incoming", number)
        mContext = context
        if (RealmUtils.isActive())
            onService(ProcessingBase.TypeCall.INC, number)
    }

    override fun onIncomingCallEnded(context: Context, number: String, start: Date, end: Date) {
        Log.d("antx", "call onIncomingCallEnded")
        // stopRecord(context, number, start, end, false)
        mContext = context
        if (RealmUtils.isActive())
            offService()
    }

    override fun onOutgoingCallStarted(context: Context, number: String, start: Date) {
        // startRecord(context, "outgoing", number)
        Log.d("antx", "call onOutgoingCallStarted")
        mContext = context
        if (RealmUtils.isActive())
            onService(ProcessingBase.TypeCall.OUT, number)
    }

    override fun onOutgoingCallEnded(context: Context, number: String, start: Date, end: Date) {
        Log.d("antx", "call onOutgoingCallEnded")
        //stopRecord(context, number, start, end, true)
        mContext = context
        if (RealmUtils.isActive())
            offService()

    }

    override fun onMissedCall(context: Context, number: String, start: Date) {
        var dateStop = CurrentTime.getLocalTime()
        if (RealmUtils.isActive())
            insertCall(number, dateStop.toString(), (0).toString(), "", null, "")
    }

    companion object {
        private val TAG = "CallRecordReceiver"
        val ACTION_IN = "android.intent.action.PHONE_STATE"
        val ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL"
        val EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER"
        private var recorder: MediaRecorder? = null
    }

    private fun insertCall(phoneNunber: String?, datecreate: String, duration: String, fileaudio: String, type: Boolean? = null, file_path: String? = "") {
        val result: java.util.HashMap<String, String> = java.util.HashMap()
        result["Authorization"] = RealmUtils.getAuthorization()
        var id = RealmUtils.getAccountId()
        val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
        mRealm.beginTransaction()
        var size = mRealm.where(LocationCurrent::class.java).findAll().size
        val locationCurrentRealm = mRealm.where(LocationCurrent::class.java).contains("idCurrent", Constant.KEY_LOCATION_CURRENT).findFirst()
        var locationCurrent: LocationCurrent? = locationCurrentRealm
        mRealm.commitTransaction()
        var message = CallLogServer(id, phoneNunber,
                datecreate, duration, locationCurrent?.lat, locationCurrent?.log, fileaudio, type.toString())
        Log.d("antx", "insertCall: " + message.toString())
        id?.let {
            Repository.createService(ApiService::class.java, result).insertCallLog(message.toMap(), Constant.KEY_API)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { _ ->
                                try {
                                   /* val fdelete = File(file_path)
                                    fdelete.delete()*/
                                } catch (e: Exception) {
                                }

                            },
                            { e ->
                                RealmUtils.saveCallLogFail(CachingCallLog(RealmUtils.idAutoIncrement(CachingCallLog::class.java), idAccount = id, phone = phoneNunber,
                                        datecreate = datecreate, duration = duration, lat = locationCurrent?.lat, lng = locationCurrent?.log, fileaudio = fileaudio, type = type.toString()))
                                Log.d("antx", "saveCallLogFail  " + e.message)
                            })
        }
    }

    private fun onService(typeCall: Int, phoneNumber: String) {
        mContext.runOnUiThread {
            val phoneCall = Intent(AccountKitController.getApplicationContext(), CallRecService::class.java)
            phoneCall.putExtra(ProcessingBase.IntentKey.PHONE_NUMBER, phoneNumber)
            phoneCall.putExtra(ProcessingBase.IntentKey.TYPE_CALL, typeCall)
            mContext.startService(phoneCall)
        }

    }

    private fun offService() {
        mContext.runOnUiThread {
            mContext.stopService(Intent(AccountKitController.getApplicationContext(), CallRecService::class.java))
        }

    }
}