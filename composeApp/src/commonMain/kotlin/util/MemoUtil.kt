package util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import model.Memo
import model.MemoRecords
import store.AppStore

class MemoUtil {
    private val memos = mutableListOf<Memo>()

    init {
        try {
            val records: MemoRecords = Json.decodeFromString(AppStore.memos)
            memos.addAll(records.memos)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addMemo(text: String, todo: Boolean, pin: Boolean) {
        val time = TimeUtil.currentTimeMillis()
        val memo = Memo(
            text = text,
            isTodo = todo,
            isPin = pin,
            createTime = time,
            modifyTime = time
        )
        memos.add(memo)
        saveToDataStore()
    }

    private fun saveToDataStore() {
        val records = MemoRecords(memos)
        AppStore.memos = Json.encodeToString(records)
        logger.i { "MemoUtil savedToDataStore, AppStore.memos=${AppStore.memos}" }
    }
}