package an.xuan.tong.historycontact.realm

import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmMigration


class CacheRealmMigration : RealmMigration {

    override fun equals(other: Any?): Boolean {
        return other === HistoryContactConfiguration
    }

    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {

    }
}