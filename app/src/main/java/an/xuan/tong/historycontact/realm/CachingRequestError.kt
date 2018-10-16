package an.xuan.tong.historycontact.realm

import an.xuan.tong.historycontact.api.model.CallLogServer
import com.squareup.moshi.Json
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class CachingCallLog(
        @PrimaryKey
        var id: Int? = 0,
        var idAccount: Int? = 0,
        var phone: String? = "",
        var datecreate: String? = "",
        var duration: String? = "",
        var lat: String? = "",
        var lng: String? = "",
        var fileaudio: String? = null,
        var type: Boolean? = false,
        var isSendToServer: Boolean? = false
) : RealmObject()

open class CachingMessage(
        @PrimaryKey
        var id: Int? = 0,
        // var toMap: Map<String, String?>? = null,
        var isUpdate: Boolean? = false
) : RealmObject()