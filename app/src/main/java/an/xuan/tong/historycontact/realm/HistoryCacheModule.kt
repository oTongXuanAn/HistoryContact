package an.xuan.tong.historycontact.realm

import an.xuan.tong.historycontact.location.LocationCurrent
import io.realm.annotations.RealmModule

@RealmModule(library = true, classes = [(ApiCaching::class), (LocationCurrent::class)])
class MixiCacheModule