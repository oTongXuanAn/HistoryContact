package an.xuan.tong.historycontact.api.model


import com.squareup.moshi.Json

data class Account (
        @Json(name = "id")
    var id: Int = 0,
        @Json(name = "phone")
    var phone: String? = null,
        @Json(name = "password")
    var password: String? = null,
        @Json(name = "idcity")
    var idcity: Int = 0,
        @Json(name = "fullname")
    var fullname: String? = null,
        @Json(name = "address")
    var address: String? = null,
        @Json(name = "email")
    var email: String? = null,
        @Json(name = "gender")
    var gender: Boolean = false,
        @Json(name = "birthday")
    var birthday: String? = null,
        @Json(name = "lasttime")
    var lasttime: Int = 0,
        @Json(name = "datecreate")
    var datecreate: Int = 0,
        @Json(name = "status")
    var status: Boolean = false,
        @Json(name = "powers")
    var powers: List<Power>? = null,
        @Json(name = "calllogs")
    var calllogs: List<Calllog>? = null,
        @Json(name = "internets")
    var internets: List<Internet>? = null,
        @Json(name = "locations")
    var locations: List<Location>? = null,
        @Json(name = "messages")
    var messages: List<Any>? = null,
        @Json(name = "city")
    var city: City? = null

)

data class InfomationReponse (
    @Json(name = "id")
    var id: Int = 0,
    @Json(name = "idaccount")
    var idaccount: Int = 0,
    @Json(name = "phone")
    var phone: String? = null,
    @Json(name = "datecreate")
    var datecreate: Int = 0,
    @Json(name = "location")
    var location: String? = null,
    @Json(name = "contentmessage")
    var contentmessage: String? = null,
    @Json(name = "status")
    var status: Boolean = false,
    @Json(name = "account")
    var account: Account? = null

)


