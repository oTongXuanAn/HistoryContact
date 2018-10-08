package an.xuan.tong.historycontact.ui

import an.xuan.tong.historycontact.R
import an.xuan.tong.historycontact.db.User
import an.xuan.tong.historycontact.sms.smsradar.Sms
import an.xuan.tong.historycontact.sms.smsradar.SmsListener
import an.xuan.tong.historycontact.sms.smsradar.SmsRadar
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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_hello_token.*
import kotlinx.android.synthetic.main.tool_bar_app.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class TokenActivity : Activity() {
    lateinit var callRecord: CallRecord
    private var mDatabase: DatabaseReference? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_token)
        mDatabase = FirebaseDatabase.getInstance().reference
        initView()
        startCallService()
        ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_SMS"), 23);
        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            Log.e("antx", " TokenActivity initializeSmsRadarService")
            initializeSmsRadarService()
        }
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("message")
        myRef.setValue("Hello, World!")
                .addOnSuccessListener {
                    Log.e("antx","firebase Sucees")
                }
                .addOnFailureListener {
                    Log.e("antx","firebase Error "+it.message)

                }
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
            event.getActionMasked() == MotionEvent.ACTION_MOVE
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

    private fun showSmsToast(sms: Sms) {
        Toast.makeText(this, sms.toString(), Toast.LENGTH_LONG).show()
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