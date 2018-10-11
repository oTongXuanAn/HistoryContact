package an.xuan.tong.historycontact.repository

import an.xuan.tong.historycontact.api.model.InformationResponse
import retrofit2.Call


interface Repository {
    fun getInfomation(phone: String, token: String): Call<InformationResponse>
}