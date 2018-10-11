package an.xuan.tong.historycontact.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ApiCaching(
        @PrimaryKey
        var apiName: String? = "",
        var data: String? = "",
        var updateAt: String? = ""

) : RealmObject()