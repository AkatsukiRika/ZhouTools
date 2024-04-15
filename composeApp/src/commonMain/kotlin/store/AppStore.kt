package store

import store.base.KotStoreModel

object AppStore : KotStoreModel() {
    var loginToken by stringStore(key = "login_token", default = "")
    var loginUsername by stringStore(key = "login_username", default = "")
    var timeCards by stringStore(key = "time_cards", default = "{}")
    var lastSync by longStore(key = "last_sync", default = 0L)
}