package an.xuan.tong.historycontact.realm

import io.realm.RealmConfiguration

/**
 *
 */
object HistoryContactConfiguration {
    private const val REALM_NAME = "historycache.realm"
    private const val SCHEMA_VERSION = 1L
    fun createBuilder(
            /*pass: ByteArray*/
    ): RealmConfiguration.Builder {
        // don't need right now
//        val md = MessageDigest.getInstance("SHA-512")
//        val encryptionKey = md.digest(pass)
        return RealmConfiguration.Builder()
                .name(REALM_NAME)
                //.encryptionKey(encryptionKey)
                .schemaVersion(SCHEMA_VERSION)
                .modules(MixiCacheModule())
                .migration(CacheRealmMigration())
    }
}