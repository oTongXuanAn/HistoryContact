package an.xuan.tong.historycontact.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PowerHistoryCaching(
        @PrimaryKey
        var datecreate: String? = "",
        var power: Boolean? = true,//true:Start -false:
        var isSend: Boolean? = false //true: Send to server

) : RealmObject()