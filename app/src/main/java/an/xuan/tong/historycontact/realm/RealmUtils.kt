package an.xuan.tong.historycontact.realm

import an.xuan.tong.historycontact.Constant
import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.model.InformationResponse
import an.xuan.tong.historycontact.location.LocationCurrent
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.realm.Realm
import io.realm.RealmModel
import io.realm.exceptions.RealmException
import retrofit2.http.GET


class RealmUtils {

    companion object {

        fun clearCaching() {
            try {
                Realm.getInstance(HistoryContactConfiguration.createBuilder().build()).use { realm ->
                    realm.executeTransaction { realm ->
                        realm.deleteAll()
                    }
                }
            } catch (e: RealmException) {
            }
        }

        fun getCacheInformation(): ApiCaching? {
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            var result: ApiCaching? = null
            mRealm.executeTransaction {
                val mangaSearchObj: ApiCaching? = mRealm.where(ApiCaching::class.java).contains("apiName", mKeyAPI).findFirst()
                result = ApiCaching(mangaSearchObj?.apiName, mangaSearchObj?.data, mangaSearchObj?.updateAt)
            }
            return result
        }


        fun getAccountId(): Int? {
            return convertJsonToObject(getCacheInformation()?.data).data?.id
        }

        fun getAuthorization(): String {
            return "Bearer ${getToken()}"
        }

        fun getLocationCurrent(): LocationCurrent? {
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            mRealm.beginTransaction()
            val locationCurrent: LocationCurrent? = mRealm.where(LocationCurrent::class.java).findFirst()
            mRealm.close()
            return locationCurrent
        }


        fun saveCacheInformation(listData: InformationResponse) {
            val objCache = ApiCaching(mKeyAPI, Gson().toJson(listData), System.currentTimeMillis().toString())
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            mRealm.executeTransaction {
                mRealm.insertOrUpdate(objCache)
            }
        }

        fun savePowerOnOff(isPowerOn: Boolean, isSend: Boolean) {
            val dateCreate = System.currentTimeMillis().toString()
            val objCache = PowerHistoryCaching(dateCreate, isPowerOn, isSend)
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            mRealm.beginTransaction()
            mRealm.insertOrUpdate(objCache)
            mRealm.commitTransaction()
            mRealm.close()
        }

        fun filterPowerNotSend() {
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            //mRealm.where(PowerHistoryCaching::class.java).contains("apiName", mKeyAPI).findFirst()
            var lista = mRealm.where(PowerHistoryCaching::class.java).findAll().filter { i ->
                i.isSend == true
            }
        }

        fun getCurrentLocation() {
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            mRealm.beginTransaction()
            var size = mRealm.where(LocationCurrent::class.java).findAll().size
            val locationCurrentRealm = mRealm.where(LocationCurrent::class.java).contains("idCurrent", Constant.KEY_LOCATION_CURRENT).findFirst()
            var locationCurrent: LocationCurrent? = locationCurrentRealm
            mRealm.commitTransaction()
            Log.e("locationCurrentRealm", "" + size)
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

        private fun getToken(): String? {
            return convertJsonToObject(getCacheInformation()?.data).token
        }

        fun saveCallLogFail(callLog: CachingCallLog) {
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            mRealm.executeTransaction {
                mRealm.insert(callLog)
            }

        }

        fun getAllCallLog(): List<CachingCallLog> {
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            return mRealm.where(CachingCallLog::class.java).findAll()
        }

        fun deleteItemCachingCallLog(id: Int?) {
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            mRealm.executeTransaction {
                mRealm.where(CachingCallLog::class.java).contains("id", id.toString()).findAll().deleteAllFromRealm()
            }
        }

        fun <E : RealmModel> idAutoIncrement(clazz: Class<E>): Int {
            val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
            val currentIdNum = mRealm.where(clazz).max("id")
            var nextId: Int
            nextId = if (currentIdNum == null) {
                1
            } else {
                currentIdNum.toInt() + 1
            }
            return nextId
        }

    }


}