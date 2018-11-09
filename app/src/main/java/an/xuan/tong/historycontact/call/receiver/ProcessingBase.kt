package an.xuan.tong.historycontact.call.receiver

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.Utils.CurrentTime
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.CallLogServer
import an.xuan.tong.historycontact.location.LocationCurrent
import an.xuan.tong.historycontact.realm.CachingCallLog
import an.xuan.tong.historycontact.realm.HistoryContactConfiguration
import an.xuan.tong.historycontact.realm.RealmUtils
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import net.callrec.library.fix.RecorderHelper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*


abstract class ProcessingBase(val context: Context) : IProcessing {
    object Constants {
        var ACTION_RECEIVER_UPDATE_NOTIFICATION: String = "ACTION_RECEIVER_UPDATE_NOTIFICATION"
    }

    protected var recorder: AudioRecorder? = null
    protected val recorderRun = RecorderRunnable()
    lateinit var recHandler: Handler
    protected var recordingStartedFlag: Boolean = false

    protected var phoneNumber: String = ""
    protected var mTypeCall: Int = 1

    protected var formatFile: String = ""
    protected var typeRecorder: TypeRecorder? = null
    protected var audioSource = -1
    protected var outputFormat: Int = 0
    protected var encoder: Int = 0
    protected var stereoChannel: Boolean = false
    protected var samplingRate: Int = 0
    protected var audioEncodingBitRate: Int = 0
    protected var filePathNoFormat: String = ""

    protected var forcedStart: Boolean = false

    abstract fun isServiceOn(): Boolean
    abstract fun getPauseBeforeRecord(): Int
    abstract fun getCheckRulesRecord(): Boolean
    abstract fun prepareAudioPreferences()
    abstract fun stopThisService()
    private fun isFirstStart(startId: Int) = startId <= 1
    lateinit var mCallStartTime: Date
    lateinit var mCallFinishTime: Date
    @Throws(ProcessingException::class)
    abstract fun makeOutputFile(phone: String, typeCall: Int): String

    @Throws(Exception::class)
    private fun startRecorder() {

        mCallStartTime = Date()
        Log.d("antx", "startRecorder: time: " + mCallStartTime.time)
        val recorderHelper = RecorderHelper.getInstance()
        var startFixWavFormat = false

        makeOutputFile(phoneNumber, mTypeCall)
        prepareAudioPreferences()

        when (typeRecorder) {
            TypeRecorder.WAV -> {
                val channelConfig = if (stereoChannel) AudioFormat.CHANNEL_IN_STEREO else AudioFormat.CHANNEL_IN_MONO
                recorder = RecorderFactory.createWavRecorder(audioSource, samplingRate, channelConfig,
                        AudioFormat.ENCODING_PCM_16BIT, filePathNoFormat)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    recorderHelper.startFixCallRecorder(context, recorder!!.audioSessionId)
                    startFixWavFormat = true
                }
            }
        }

        recorder!!.start()

        recordingStartedFlag = true

        onStartRecord()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && startFixWavFormat) {
            recorderHelper.stopFixCallRecorder()
        }
    }

    private fun stopRecorder() {
        Log.d("antx", "stopRecorder")
        mCallFinishTime = Date()
        Log.d("antx", "startRecorder: time: " + mCallFinishTime.time)
        if (recorder == null) return

        if (recorder!!.isRecorded()) {
            recorder!!.stop()
            onStopRecord()
        }
    }

    protected open fun prepareService(intent: Intent) {
        phoneNumber = intent.getStringExtra(IntentKey.PHONE_NUMBER)
        mTypeCall = intent.getIntExtra(IntentKey.TYPE_CALL, -1)
        Log.d("antx", "prepareService: " + phoneNumber + " " + mTypeCall)
    }

    protected open fun handleFirstStart(intent: Intent): Int {
        Log.d("antx", "handleFirstStart" + intent.getStringExtra(Constant.PHONE_NUMBER))
        prepareService(intent)

        if (forcedStart) {
            startRecord(0)
        } else {
            getPauseBeforeRecord()

            if (!getCheckRulesRecord()) {
                onCheckRulesRecord(false)
                return Service.START_NOT_STICKY
            }

            startRecord(getPauseBeforeRecord() * 1000)
        }

        return Service.START_REDELIVER_INTENT
    }

    protected open fun handleNoFirstStart(intent: Intent) {
        if (forcedStart) {
            if (recorder == null || recorder!!.isRecorded()) {
                startRecord(0)
            }
        }
    }

    protected open fun startRecord(delayMS: Int) {
        recHandler.removeCallbacks(recorderRun)
        Log.d("antx", "startRecord")
        onPreStartRecord()

        if (delayMS == 0) {
            recHandler.post(recorderRun)
        } else {
            recHandler.postDelayed(recorderRun, delayMS.toLong())
            onWaitStartRecord()
        }
    }

    protected open fun stopRecord() {
        recHandler.removeCallbacks(recorderRun)
        stopRecorder()
        var typeCall: Boolean
        typeCall = mTypeCall != 1
        Log.d("antx", "stopRecord: " + filePathNoFormat + "typeCall" + typeCall)
        sendRecoderToServer(filePathNoFormat, phoneNumber, mCallStartTime, mCallFinishTime, typeCall)
    }

    open protected fun onCheckRulesRecord(check: Boolean) {}
    open protected fun onWaitStartRecord() {}
    open protected fun onStartRecord() {}
    open protected fun onStopRecord() {}
    open protected fun onRecorderError(e: Exception) {}
    open protected fun onRecorderError(e: RecorderBase.RecorderException) {}

    open protected fun onRecorderError(e: ProcessingException) {}

    open protected fun onPreStartRecord() {
    }

    override fun onCreate() {
        recHandler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!isServiceOn()) {
            stopThisService()
            return Service.START_REDELIVER_INTENT
        }

        forcedStart = intent.getBooleanExtra(IntentKey.FORCED_START, false)


        if (isFirstStart(startId)) return handleFirstStart(intent)
        handleNoFirstStart(intent)
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        stopRecord()
    }

    public object IntentKey {
        val PHONE_NUMBER = "PHONE_NUMBER"
        val TYPE_CALL = "TYPE_CALL"
        val FORCED_START = "FORCED_START"
    }

    public object TypeCall {
        val INC = 1
        val OUT = 2
    }

    enum class TypeRecorder { WAV }

    inner class RecorderRunnable : Runnable {
        override fun run() {
            try {
                Log.d("antx", "RecorderRunnable")
                startRecorder()
            } catch (e: RecorderBase.RecorderException) {
                e.printStackTrace()
                onRecorderError(e)
                stopThisService()
            } catch (e: ProcessingException) {
                e.printStackTrace()
                onRecorderError(e)
                stopThisService()
            }
        }
    }

    public class ProcessingException : Exception {
        val code: Int

        constructor(message: String, codeError: Int) : super(message) {
            this.code = codeError
        }

        constructor(message: String, throwable: Throwable, codeError: Int) : super(message, throwable) {
            this.code = codeError
        }
    }

    private fun sendRecoderToServer(filePath: String, number: String, startDate: Date, endDate: Date, typeCall: Boolean) {
        try {
            val file = File(filePath)
            val result: HashMap<String, String> = HashMap()
            result["Authorization"] = RealmUtils.getAuthorization()
            var id = RealmUtils.getAccountId()
            val temp = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            var imageFile = MultipartBody.Part.createFormData(file.name, file.name, temp)
            Log.d("antx", "insertCall: " + file.name)
            Repository.createService(ApiService::class.java, result).insertUpload(Constant.KEY_API, id, imageFile)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                if (result.isNotEmpty()) {
                                    var diffInMs = endDate.time - startDate.time
                                    var diffInSec = diffInMs / 1000
                                    var dateStop = CurrentTime.getLocalTime()
                                    insertCall(number, dateStop.toString(), (diffInSec).toString(), result[0], typeCall, filePath)
                                }
                            },
                            { e ->
                                var diffInMs = endDate.time - startDate.time
                                var diffInSec = diffInMs / 1000
                                var dateStop = CurrentTime.getLocalTime()
                                insertCall(number, dateStop.toString(), (diffInSec).toString(), filePath, typeCall, filePath)
                            })
        } catch (e: Exception) {
            Log.d("antx Exception", "sendRcoderToServer " + e.message)
        }

    }

    private fun insertCall(phoneNunber: String?, datecreate: String, duration: String, fileaudio: String, type: Boolean? = null, file_path: String? = "") {
        val result: HashMap<String, String> = HashMap()
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

                                /*try {
                                    val fdelete = File(file_path)
                                    fdelete.delete()
                                } catch (e: Exception) {

                                }*/

                            },
                            { e ->
                                RealmUtils.saveCallLogFail(CachingCallLog(RealmUtils.idAutoIncrement(CachingCallLog::class.java), idAccount = id, phone = phoneNunber,
                                        datecreate = datecreate, duration = duration, lat = locationCurrent?.lat, lng = locationCurrent?.log, fileaudio = fileaudio, type = type.toString()))
                                Log.d("antx", "saveCallLogFail  " + e.message)
                            })
        }
    }

}