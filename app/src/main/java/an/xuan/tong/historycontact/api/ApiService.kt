package an.xuan.tong.historycontact.api

import an.xuan.tong.historycontact.api.model.TokenReponse
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    //?api={api}&phone={phone}
    @GET("api/account/active")
    fun getInfomation(@Query("api") api: String,
                      @Query("phone") phone: String): Observable<TokenReponse>
}