package an.xuan.tong.historycontact.api.config

import an.xuan.tong.historycontact.api.ApiService
import com.squareup.moshi.Moshi
import retrofit2.Retrofit

interface ApiConfigurable {
    val moshi: Moshi
    val retrofit: Retrofit
    val mangaService: ApiService
}