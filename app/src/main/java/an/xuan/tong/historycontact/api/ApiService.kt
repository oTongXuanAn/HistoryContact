package an.xuan.tong.historycontact.api

import an.xuan.tong.historycontact.api.model.Account
import an.xuan.tong.historycontact.api.model.CallSMSReponse
import an.xuan.tong.historycontact.api.model.InformationResponse
import an.xuan.tong.historycontact.api.model.Message
import com.squareup.moshi.Json
import io.reactivex.Observable
import retrofit2.http.*

interface ApiService {
    //?api={api}&phone={phone}
    @GET("/api/account/active")
    fun getInfomation(@Query("api") api: String,
                      @Query("phone") phone: String): Observable<InformationResponse>

    //api/location/insert?api={api}

    @POST("/api/location/insert")
    fun insertLocation(@Query("api") apikey: Int,
                       @Query("idaccount") idaccount: Int,
                       @Query("datecreate") datecreate: Int,
                       @Query("lat") lat: Long,
                       @Query("lng") lng: Long,
                       @Query("account") account: Account): Observable<CallSMSReponse>


    @POST("/api/message/insert")
    fun insertMessage(@Body message: Message,
                      @Query("api") api: String): Observable<CallSMSReponse>


    @POST("/api/calllog/insert?api={api}")
    fun insertCallLog(@Query("id") id: Int,
                      @Query("idaccount") idaccount: Int,
                      @Query("phone") phone: String,
                      @Query("datecreate") datecreate: Long,
                      @Query("duration") duration: Long,
                      @Query("location") location: String,
                      @Query("fileaudio") fileaudio: String,
                      @Query("status") status: Boolean,
                      @Query("account") account: Account): Observable<CallSMSReponse>

    @POST("/api/power/insert?api={api}")
    fun insertPower(@Query("id") id: Int,
                    @Query("idaccount") idaccount: Int,
                    @Query("datecreate") datecreate: Long,
                    @Query("status") status: Boolean,
                    @Query("account") account: Account): Observable<CallSMSReponse>

}