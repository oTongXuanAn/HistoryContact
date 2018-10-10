package an.xuan.tong.historycontact.api.model

import com.squareup.moshi.Json

data class CallSMSReponse(
        @Json(name = "status")
        val status: String? = null,
        @Json(name = "message")
        val message: String? = null,
        @Json(name = "id")
        val id: String
)