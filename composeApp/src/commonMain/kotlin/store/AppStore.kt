package store

import store.base.KotStoreModel

object AppStore : KotStoreModel() {
    var loginToken by stringStore(key = "login_token", default = "")
    var loginUsername by stringStore(key = "login_username", default = "")
    var customServerUrl by stringStore(key = "custom_server_url", default = "")
    var timeCards by stringStore(key = "time_cards", default = "{}", syncSave = true)
    var memos by stringStore(key = "memos", default = "{}", syncSave = true)
    var schedules by stringStore(key = "schedules", default = "{}", syncSave = true)
    var lastSync by longStore(key = "last_sync", default = 0L)

    fun clearCache() {
        customServerUrl = ""
        timeCards = "{}"
        memos = "{}"
        schedules = "{}"
        lastSync = 0L
    }
}