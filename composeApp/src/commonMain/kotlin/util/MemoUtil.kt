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

    fun modifyMemo(memo: Memo, text: String, todo: Boolean, pin: Boolean) {
        val findResult = memos.find {
            it.text == memo.text
                    && it.isTodo == memo.isTodo
                    && it.isPin == memo.isPin
                    && it.createTime == memo.createTime
                    && it.modifyTime == memo.modifyTime
        }
        if (findResult != null) {
            findResult.text = text
            findResult.isTodo = todo
            findResult.isPin = pin
            findResult.modifyTime = TimeUtil.currentTimeMillis()
        } else {
            addMemo(text, todo, pin)
        }
        saveToDataStore()
    }

    fun deleteMemo(memo: Memo) {
        memos.remove(memo)
        saveToDataStore()
    }

    fun getDisplayList(): List<Memo> {
        val displayList = mutableListOf<Memo>()
        val pinList = memos.filter { it.isPin }
        val notPinList = memos.filterNot { it.isPin }
        displayList.addAll(pinList)
        displayList.addAll(notPinList)
        return displayList
    }

    private fun saveToDataStore() {
        val records = MemoRecords(memos)
        AppStore.memos = Json.encodeToString(records)
        logger.i { "MemoUtil savedToDataStore, AppStore.memos=${AppStore.memos}" }
    }
}