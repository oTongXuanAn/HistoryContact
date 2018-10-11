package an.xuan.tong.historycontact.realm

import an.xuan.tong.historycontact.api.model.InformationResponse
import an.xuan.tong.historycontact.api.model.Power
import com.google.gson.Gson
import io.realm.Realm
import io.realm.exceptions.RealmException

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

fun getCacheInformation(mKeyAPI: String): ApiCaching? {
    val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
    val mangaSearchObj: ApiCaching? = mRealm.where(ApiCaching::class.java).contains("apiName", mKeyAPI).findFirst()
    // clone data if don't have this line -> crash app after "mRealm.close()"
    val result = ApiCaching(mangaSearchObj?.apiName, mangaSearchObj?.data, mangaSearchObj?.updateAt)
    mRealm.close()
    return result
}

private fun saveCacheInformation(listData: InformationResponse, mKeyAPI: String) {
    val objCache = ApiCaching(mKeyAPI, Gson().toJson(listData), System.currentTimeMillis().toString())
    val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
    mRealm.beginTransaction()
    mRealm.insertOrUpdate(objCache)
    mRealm.commitTransaction()
    mRealm.close()
}

private fun savePowerOnOff(isPowerOn: Boolean, isSend: Boolean) {
    val dateCreate = System.currentTimeMillis().toString()
    val objCache = PowerHistoryCaching(dateCreate, isPowerOn, isSend)
    val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
    mRealm.beginTransaction()
    mRealm.insertOrUpdate(objCache)
    mRealm.commitTransaction()
    mRealm.close()
}

private fun filterPowerNotSend() {
    val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
    //mRealm.where(PowerHistoryCaching::class.java).contains("apiName", mKeyAPI).findFirst()
    mRealm.where(PowerHistoryCaching::class.java).findAll()

}