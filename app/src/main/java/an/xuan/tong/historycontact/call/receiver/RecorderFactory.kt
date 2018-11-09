package an.xuan.tong.historycontact.call.receiver


object RecorderFactory {
    fun createWavRecorder(audioSource: Int, sampleRateInHz: Int, channelConfig: Int, audioEncoding: Int,
                          filePathNoFormat: String): AudioRecorder {
        return WavRecorder(audioSource, sampleRateInHz, channelConfig, audioEncoding, filePathNoFormat)
    }
}
