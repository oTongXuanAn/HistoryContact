package an.xuan.tong.historycontact.realm

import io.realm.annotations.RealmModule

@RealmModule(library = true, classes = [(ApiCaching::class)])
class MixiCacheModule