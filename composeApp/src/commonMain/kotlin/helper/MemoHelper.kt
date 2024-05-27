package helper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.records.Memo
import model.records.MemoRecords
import model.request.MemoSyncRequest
import store.AppStore
import util.TimeUtil

object MemoHelper {
    fun addMemo(text: String, todo: Boolean, pin: Boolean) {
        val time = TimeUtil.currentTimeMillis()
        val memo = Memo(
            text = text,
            isTodo = todo,
            isPin = pin,
            createTime = time,
            modifyTime = time
        )
        val memos = getMemos()
        memos.add(memo)
        saveMemos(memos)
    }

    fun modifyMemo(memo: Memo, text: String, todo: Boolean, pin: Boolean) {
        val memos = getMemos()
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
        saveMemos(memos)
    }

    fun markDone(memo: Memo, done: Boolean) {
        val memos = getMemos()
        val findResult = memos.find {
            it.text == memo.text
                    && it.isTodo == memo.isTodo
                    && it.isPin == memo.isPin
                    && it.createTime == memo.createTime
                    && it.modifyTime == memo.modifyTime
        }
        if (findResult != null) {
            findResult.isTodoFinished = done
            saveMemos(memos)
        }
    }

    fun deleteMemo(memo: Memo) {
        val memos = getMemos()
        memos.remove(memo)
        saveMemos(memos)
    }

    fun getDisplayList(): List<Memo> {
        val memos = getMemos()
        val displayList = mutableListOf<Memo>()
        val pinList = memos.filter { it.isPin }
        val notPinList = memos.filterNot { it.isPin }
        displayList.addAll(pinList)
        displayList.addAll(notPinList)
        return displayList
    }

    fun buildSyncRequest(): MemoSyncRequest? {
        val memos = getMemos()
        return try {
            if (AppStore.loginUsername.isEmpty()) {
                null
            } else {
                MemoSyncRequest(username = AppStore.loginUsername, memos = memos)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getMemos() = try {
        val memoRecords = Json.decodeFromString<MemoRecords>(AppStore.memos)
        memoRecords.memos
    } catch (e: Exception) {
        e.printStackTrace()
        mutableListOf()
    }

    private fun saveMemos(memos: MutableList<Memo>) {
        val records = MemoRecords(memos)
        AppStore.memos = Json.encodeToString(records)
    }
}