package net.callrec.app

import an.xuan.tong.historycontact.callcontact.AudioRecorder
import an.xuan.tong.historycontact.callcontact.WavRecorder


/**
 * Created by Viktor Degtyarev on 16.10.17
 * E-mail: viktor@degtyarev.biz
 */
object RecorderFactory {
    fun createWavRecorder(audioSource: Int, sampleRateInHz: Int, channelConfig: Int, audioEncoding: Int,
                          filePathNoFormat: String): AudioRecorder {
        return WavRecorder(audioSource, sampleRateInHz, channelConfig, audioEncoding, filePathNoFormat)
    }
}
