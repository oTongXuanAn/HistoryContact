package com.aykuttasil.callrecord.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.Toast;

import com.aykuttasil.callrecord.CallRecord;
import com.aykuttasil.callrecord.helper.PrefsHelper;

/**
 * Created by aykutasil on 19.10.2016.
 */

public class CallRecordService extends Service {

    private static final String TAG = CallRecordService.class.getSimpleName();

    protected CallRecord mCallRecord;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG + "antx", "onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG + "antx", "onStartCommand()");

        String file_name = PrefsHelper.readPrefString(this, CallRecord.PREF_FILE_NAME);
        String dir_path = PrefsHelper.readPrefString(this, CallRecord.PREF_DIR_PATH);
        String dir_name = PrefsHelper.readPrefString(this, CallRecord.PREF_DIR_NAME);
        boolean show_seed = PrefsHelper.readPrefBool(this, CallRecord.PREF_SHOW_SEED);
        boolean show_phone_number = PrefsHelper.readPrefBool(this, CallRecord.PREF_SHOW_PHONE_NUMBER);
        int output_format = PrefsHelper.readPrefInt(this, CallRecord.PREF_OUTPUT_FORMAT);
        int audio_source = PrefsHelper.readPrefInt(this, CallRecord.PREF_AUDIO_SOURCE);
        int audio_encoder = PrefsHelper.readPrefInt(this, CallRecord.PREF_AUDIO_ENCODER);

        mCallRecord = new CallRecord.Builder(this)
                .setRecordFileName(file_name)
                .setRecordDirName(dir_name)
                .setRecordDirPath(dir_path)
                .setAudioEncoder(audio_encoder)
                .setAudioSource(audio_source)
                .setOutputFormat(output_format)
                .setShowSeed(show_seed)
                .setShowPhoneNumber(show_phone_number)
                .build();
        mCallRecord.startCallReceiver();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallRecord.stopCallReceiver();

        Log.e(TAG + "antx", "onDestroy()");
    }
}
