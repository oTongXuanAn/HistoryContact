package an.xuan.tong.historycontact.call.receiver


import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import java.io.File


/**
 * Created by Viktor Degtyarev on 16.10.17
 * E-mail: viktor@degtyarev.biz
 */
class CallRecProcessingNotification(service: Service) : ProcessingBaseNotification(service) {
    override fun getNotificationUpdate(): INotification<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNotificationWait(): INotification<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNotificationErr(e: RecorderBase.RecorderException): INotification<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNotificationErr(e: ProcessingException): INotification<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun makeOutputFile(phone:String,typeCall:Int): String {
        val dirStorage = Utils.getDefaultPath(context)

        val file = File(dirStorage)

        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw Exception()
            }
        }

        filePathNoFormat = dirStorage + Utils.makeFileName(phone,typeCall)
        return filePathNoFormat
    }

    override fun getNotificationOk(): INotification<*> {
        return CallRecNotification(this)
    }

    override fun getNotificationErr(e: Exception): INotification<*> {
        return CallRecNotificationErr(this)
    }

    override fun isServiceOn(): Boolean {
        return true
    }

    override fun getPauseBeforeRecord(): Int {
        return 0
    }

    override fun getCheckRulesRecord(): Boolean {
        return true
    }

    override fun prepareAudioPreferences() {
        formatFile = "wav"
        audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION
        outputFormat = 0
        encoder = 0
        stereoChannel = false
        samplingRate = 8000
        audioEncodingBitRate = 0
        typeRecorder = TypeRecorder.WAV
    }

    override fun stopThisService() {
        service.stopService(Intent(context, service.javaClass))
    }


}