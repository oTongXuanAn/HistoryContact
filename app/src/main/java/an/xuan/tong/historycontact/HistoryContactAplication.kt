package an.xuan.tong.historycontact

import android.app.Application
import android.app.NativeActivity
import android.os.Build
import android.os.StrictMode
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import io.realm.Realm
import io.realm.RealmConfiguration
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import com.getkeepsafe.relinker.ReLinker

class HistoryContactAplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        Fabric.with(this, Crashlytics())
        ReLinker.loadLibrary(this, "acr", object : ReLinker.LoadListener {
            override fun success() { /* Yay */
                Log.e("antx","loadLibrary success")
            }

            override fun failure(t: Throwable) { /* Boo */
                Log.e("antx","loadLibrary error"+ t.message)
            }
        })
        System.loadLibrary("acr");

    }

    fun enableStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build())
    }


}