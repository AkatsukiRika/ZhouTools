package store

import store.base.KotStoreModel

object AppStore : KotStoreModel() {
    var loginToken by stringStore(key = "login_token", default = "")
}