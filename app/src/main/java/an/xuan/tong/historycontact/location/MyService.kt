package an.xuan.tong.historycontact.location

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log

import org.json.JSONObject

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.InformationResponse
import an.xuan.tong.historycontact.api.model.SendLocation
import an.xuan.tong.historycontact.realm.ApiCaching
import an.xuan.tong.historycontact.realm.HistoryContactConfiguration
import an.xuan.tong.historycontact.realm.getCacheInformation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

class MyService : Service() {
    var mLocationManager: LocationManager? = null

    internal var mLocationListeners = arrayOf(LocationListener(LocationManager.GPS_PROVIDER), LocationListener(LocationManager.NETWORK_PROVIDER))

    inner class LocationListener(provider: String) : android.location.LocationListener {
        var mLastLocation: Location

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")
            mLastLocation.set(location)
            saveLocation(mLastLocation.latitude, mLastLocation.longitude)
            var id = convertJsonToObject(getCacheInformation()?.data).data?.id
            var timeCreate = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            val result: HashMap<String, String> = HashMap()
            val token = convertJsonToObject(getCacheInformation()?.data).token
            result.put("Authorization", "Bearer $token")
            result.put("Content-Type", "application/json")
            var sendLocation = SendLocation(id, timeCreate.toString(), mLastLocation.latitude.toString(), mLastLocation.longitude.toString())
            Log.e("sendLocation", " " + sendLocation.toString())
            Repository.createService(ApiService::class.java, result).insertLocation(Constant.KEY_API, sendLocation.toMap())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                Log.e("antx", "insertLocation  " + result.toString())

                            },
                            { e ->
                                Log.e("test", "insertLocation error " + e.message)
                            })
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }
    }

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        initializeLocationManager()
        try {
            mLocationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                    mLocationListeners[1])
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        try {
            mLocationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                    mLocationListeners[0])
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }

    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        for (i in mLocationListeners.indices) {
            try {
                mLocationManager?.removeUpdates(mLocationListeners[i])
            } catch (ex: Exception) {
                Log.i(TAG, "fail to remove location listners, ignore", ex)
            }


        }
    }

    private fun initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager")
        mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    companion object {
        private val TAG = "BOOMBOOMTESTGPS"
        private val LOCATION_INTERVAL = 60000
        private val LOCATION_DISTANCE = 100f
    }

    private fun getCacheInformation(): ApiCaching? {
        val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
        val mangaSearchObj: ApiCaching? = mRealm.where(ApiCaching::class.java).contains("apiName", mKeyAPI).findFirst()
        // clone data if don't have this line -> crash app after "mRealm.close()"
        val result = ApiCaching(mangaSearchObj?.apiName, mangaSearchObj?.data, mangaSearchObj?.updateAt)
        mRealm.close()
        return result
    }

    private fun saveLocation(lat: Double, lng: Double) {
        val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
        mRealm.beginTransaction()
        Log.e("saveLocation: ", "lat: " + lat.toString() + " " + lng.toString())
        var locationCurrent = LocationCurrent(null, lat.toString(), lng.toString())
        mRealm.insertOrUpdate(locationCurrent)
        mRealm.close()
    }

    private val mKeyAPI: String by lazy {
        // Get Value of annotation API for save cache as KEY_CACHE
        val method = ApiService::getInfomation
        val get = method.annotations.find { it is GET } as? GET
        get?.value + ""
    }

    private fun convertJsonToObject(json: String?): InformationResponse {
        return Gson().fromJson(json, object : TypeToken<InformationResponse?>() {}.type)
    }

}