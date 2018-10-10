package an.xuan.tong.historycontact.api

import an.xuan.tong.historycontact.api.model.Account
import an.xuan.tong.historycontact.api.model.TokenReponse
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    //?api={api}&phone={phone}
    @GET("api/account/active")
    fun getInfomation(@Query("api") api: String,
                      @Query("phone") phone: String): Observable<TokenReponse>

    //api/location/insert?api={api}
    @POST("api/location/insert")
    fun insertLocation(@Query("id") id: Int,
                       @Query("idaccount") idaccount: Int,
                       @Query("datecreate") datecreate: Int,
                       @Query("lat") lat: Long,
                       @Query("lng") lng: Long,
                       @Query("account") account: Account)

    //http://api.naxuto.com/Help/Api/POST-api-location-insert_api

}