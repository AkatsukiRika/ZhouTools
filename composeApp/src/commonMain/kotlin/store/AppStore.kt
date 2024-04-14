package store

import store.base.KotStoreModel

object AppStore : KotStoreModel() {
    var loginToken by stringStore(key = "login_token", default = "")
    var timeCards by stringStore(key = "time_cards", default = "{}")
}